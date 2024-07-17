package com.jd.jsf.gd.filter;


import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.ProviderConfig;
import com.jd.jsf.gd.config.ext.ExtensibleClass;
import com.jd.jsf.gd.config.ext.ExtensionLoader;
import com.jd.jsf.gd.config.ext.ExtensionLoaderFactory;
import com.jd.jsf.gd.error.InitErrorException;
import com.jd.jsf.gd.msg.RequestMessage;
import com.jd.jsf.gd.msg.ResponseMessage;
import com.jd.jsf.gd.util.CommonUtils;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.jsf.gd.util.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterChain {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterChain.class);
    private Filter chain;
    private FilterResponse responseChain;
    private Map<String, Object> configContext;

    private FilterChain(List<AbstractFilter> filters, Filter lastFilter, Map<String, Object> context) {
        this.chain = lastFilter;
        AbstractFilter preResponse = null;
        if (lastFilter instanceof FilterResponse) {
            this.responseChain = (FilterResponse)lastFilter;
            preResponse = (AbstractFilter)lastFilter;
        }

        this.configContext = context;
        if (CommonUtils.isNotEmpty(filters)) {
            for(int i = filters.size() - 1; i >= 0; --i) {
                try {
                    AbstractFilter filter = (AbstractFilter)filters.get(i);
                    if (filter.getNext() != null) {
                        LOGGER.warn("[JSF-22000]Filter {} has been already used, maybe it's singleton, jsf will try to clone new instance instead of it", filter);
                        filter = (AbstractFilter)filter.clone();
                    }

                    filter.setNext(this.chain);
                    filter.setConfigContext(this.configContext);
                    this.chain = filter;
                    if (filter instanceof FilterResponse) {
                        if (null == preResponse) {
                            this.responseChain = (FilterResponse)filter;
                        } else {
                            preResponse.setPrevReponse((FilterResponse)filter);
                        }

                        preResponse = filter;
                    }
                } catch (Exception var7) {
                    LOGGER.error("加载filter列表异常", var7);
                    throw new InitErrorException("加载filter列表异常", var7);
                }
            }
        }

    }

    public static synchronized FilterChain buildChain(List<AbstractFilter> filters, Filter lastFilter, Map<String, Object> context) {
        return new FilterChain(filters, lastFilter, context);
    }

    public static FilterChain buildProviderChain(ProviderConfig providerConfig, Filter lastFilter) {
        Map<String, Object> context = providerConfig.getConfigValueCache(true);
        List<AbstractFilter> customFilters = providerConfig.getFilter() == null ? null : new CopyOnWriteArrayList(providerConfig.getFilter());
        HashSet<String> excludes = parseExcludeFilter(customFilters);
        List<AbstractFilter> filters = new ArrayList();
        if (!excludes.contains("*") && !excludes.contains("default")) {
            if (CommonUtils.isLinux() && !CommonUtils.isFalse(JSFContext.getGlobalVal("check.system.time", "true"))) {
                filters.add(new SystemTimeCheckFilter());
            }

            if (!excludes.contains("exception")) {
                filters.add(new ProviderExceptionFilter());
            }

            filters.add(new ProviderContextFilter());
            if (!excludes.contains("providerGeneric")) {
                filters.add(new ProviderGenericFilter());
            }

            filters.add(new ProviderUnitValidationFilter());
            if (!excludes.contains("providerHttpGW")) {
                filters.add(new ProviderHttpGWFilter());
            }

            if (!excludes.contains("providerLimiter")) {
                filters.add(new ProviderInvokeLimitFilter());
            }

            if (providerConfig.hasToken() && !excludes.contains("token")) {
                filters.add(new TokenFilter());
            }

            if (!excludes.contains("providerMethodCheck")) {
                filters.add(new ProviderMethodCheckFilter(providerConfig));
            }

            if (!excludes.contains("providerTimeout")) {
                filters.add(new ProviderTimeoutFilter());
            }

            if (providerConfig.hasValidation() && !excludes.contains("validation")) {
                filters.add(new ValidationFilter());
            }

            if (providerConfig.hasCache() && !excludes.contains("cache")) {
                filters.add(new CacheFilter(providerConfig));
            }

            if (providerConfig.hasConcurrents() && !excludes.contains("providerConcurrents")) {
                filters.add(new ProviderConcurrentsFilter(providerConfig));
            }

            if (!excludes.contains("providerSecurity")) {
                filters.add(new ProviderSecurityFilter());
            }
        }

        if (CommonUtils.isNotEmpty(customFilters)) {
            filters.addAll(customFilters);
        }

        ExtensionLoader<Filter> extensionLoader = ExtensionLoaderFactory.getExtensionLoader(Filter.class);
        if (extensionLoader != null) {
            Iterator var7 = extensionLoader.getProviderSideAutoActives().iterator();

            while(var7.hasNext()) {
                ExtensibleClass<Filter> extensibleClass = (ExtensibleClass)var7.next();
                Filter filter = (Filter)extensibleClass.getExtInstance();
                if (filter != null && filter instanceof AbstractFilter) {
                    LOGGER.info("load provider extension filter:{}", filter.getClass().getName());
                    filters.add((AbstractFilter)filter);
                }
            }
        }

        return buildChain(filters, lastFilter, context);
    }

    public static FilterChain buildConsumerChain(ConsumerConfig consumerConfig, Filter lastFilter) {
        Map<String, Object> context = consumerConfig.getConfigValueCache(true);
        List<AbstractFilter> customFilters = consumerConfig.getFilter() == null ? null : new CopyOnWriteArrayList(consumerConfig.getFilter());
        HashSet<String> excludes = parseExcludeFilter(customFilters);
        List<AbstractFilter> filters = new ArrayList();
        if (!excludes.contains("*") && !excludes.contains("default")) {
            if (CommonUtils.isLinux() && !excludes.contains("systemTimeCheck") && !CommonUtils.isFalse(JSFContext.getGlobalVal("check.system.time", "true"))) {
                filters.add(new SystemTimeCheckFilter());
            }

            if (!excludes.contains("exception")) {
                filters.add(new ConsumerExceptionFilter());
            }

            if (consumerConfig.isGeneric() && !excludes.contains("consumerGeneric")) {
                filters.add(new ConsumerGenericFilter());
            }

            filters.add(new ConsumerContextFilter());
            if (consumerConfig.hasCache() && !excludes.contains("cache")) {
                filters.add(new CacheFilter(consumerConfig));
            }

            if (!excludes.contains("mock")) {
                filters.add(new MockFilter(consumerConfig));
            }

            filters.add(new ConsumerMonitorFilter());
            if (JSFContext.get("appId") != null && !excludes.contains("consumerInvokeLimit")) {
                filters.add(new ConsumerInvokeLimitFilter());
            }

            if (consumerConfig.hasValidation() && !excludes.contains("validation")) {
                filters.add(new ValidationFilter());
            }

            if (consumerConfig.hasConcurrents() && !excludes.contains("consumerConcurrents")) {
                filters.add(new ConsumerConcurrentsFilter(consumerConfig));
            }
        }

        if (CommonUtils.isNotEmpty(customFilters)) {
            filters.addAll(customFilters);
        }

        ExtensionLoader<Filter> extensionLoader = ExtensionLoaderFactory.getExtensionLoader(Filter.class);
        if (extensionLoader != null) {
            Iterator var7 = extensionLoader.getConsumerSideAutoActives().iterator();

            while(var7.hasNext()) {
                ExtensibleClass<Filter> extensibleClass = (ExtensibleClass)var7.next();
                Filter filter = (Filter)extensibleClass.getExtInstance();
                if (filter != null && filter instanceof AbstractFilter) {
                    LOGGER.info("load consumer extension filter:{}", filter.getClass().getName());
                    filters.add((AbstractFilter)filter);
                }
            }
        }

        return buildChain(filters, lastFilter, context);
    }

    private static HashSet<String> parseExcludeFilter(List<AbstractFilter> customFilters) {
        HashSet<String> excludeKeys = new HashSet();
        if (CommonUtils.isNotEmpty(customFilters)) {
            Iterator var2 = customFilters.iterator();

            while(var2.hasNext()) {
                AbstractFilter filter = (AbstractFilter)var2.next();
                if (filter instanceof ExcludeFilter) {
                    ExcludeFilter excludeFilter = (ExcludeFilter)filter;
                    String excludeFilterName = excludeFilter.getExcludeFilterName().substring(1);
                    if (StringUtils.isNotEmpty(excludeFilterName)) {
                        excludeKeys.add(excludeFilterName);
                    }

                    customFilters.remove(filter);
                }
            }
        }

        if (!excludeKeys.isEmpty()) {
            LOGGER.info("Find exclude filters: {}", excludeKeys);
        }

        return excludeKeys;
    }

    public ResponseMessage invoke(RequestMessage requestMessage) {
        if(requestMessage.getInvocationBody() != null
                && requestMessage.getInvocationBody().getAttachment("testUrlAddress") !=null
        ){
            requestMessage.getMsgHeader().getAttrMap().put(java.lang.Byte.valueOf((byte)100),requestMessage.getInvocationBody().getAttachment("testUrlAddress"));
            requestMessage.getInvocationBody().getAttachments().remove("testUrlAddress");
        }else if(requestMessage.getInvocationBody() != null
                && requestMessage.getInvocationBody().getAttachment("testInterfaceAndAlias") !=null){
            requestMessage.getMsgHeader().getAttrMap().put(java.lang.Byte.valueOf((byte)101),requestMessage.getInvocationBody().getAttachment("testInterfaceAndAlias"));
            requestMessage.getInvocationBody().getAttachments().remove("testInterfaceAndAlias");
        }

        return this.getChain().invoke(requestMessage);
    }

    public ResponseMessage onResponse(RequestMessage requestMessage, ResponseMessage responseMessage) {
        return this.getResponseChain() == null ? responseMessage : this.getResponseChain().onResponse(requestMessage, responseMessage);
    }

    protected Filter getChain() {
        return this.chain;
    }

    protected FilterResponse getResponseChain() {
        return this.responseChain;
    }
}