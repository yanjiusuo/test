package com.jd.jsf.gd.config;


import com.jd.jsf.gd.client.Client;
import com.jd.jsf.gd.client.ClientFactory;
import com.jd.jsf.gd.client.ClientProxyInvoker;
import com.jd.jsf.gd.error.InitErrorException;
import com.jd.jsf.gd.error.NoRouterException;
import com.jd.jsf.gd.protocol.ProtocolFactory;
import com.jd.jsf.gd.reflect.ProxyFactory;
import com.jd.jsf.gd.registry.ClientRegistry;
import com.jd.jsf.gd.registry.ConfigListener;
import com.jd.jsf.gd.registry.Provider;
import com.jd.jsf.gd.registry.ProviderListener;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.jsf.gd.server.BaseServerHandler;
import com.jd.jsf.gd.transport.CallbackUtil;
import com.jd.jsf.gd.util.CodecUtils;
import com.jd.jsf.gd.util.CommonUtils;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.jsf.gd.util.RpcStatus;
import com.jd.jsf.gd.util.SecurityContext;
import com.jd.jsf.gd.util.StringUtils;
import com.jd.jsf.gd.util.Constants.CodecType;
import com.jd.jsf.gd.util.Constants.ProtocolType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerConfig<T> extends AbstractConsumerConfig<T> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerConfig.class);
    private static final long serialVersionUID = -2910502986608678372L;
    protected transient volatile Client client;
    protected transient volatile ProviderListener providerListener;

    public ConsumerConfig() {
    }



    private void preRefer(){
        if(!"com.jd.jsf.service.RegistryService".equals(this.interfaceId) ){
            String useProxy = getParameter("proxy.enableProxy");
            if(StringUtils.isNotBlank(useProxy)){
                getParameters().remove("proxy.enableProxy");
                if(this.url !=null && !"".equals(this.url)){
                    com.jd.jsf.gd.registry.Provider provider = com.jd.jsf.gd.registry.Provider.valueOf(this.url);
                    this.setParameter("testUrlAddress",provider.getIp()+":"+provider.getPort());

                    String beforeUrl = this.url;
                    if(this.url.indexOf("?")==-1){
                        this.url = "jsf://test-local-debug.jd.local:19999";
                    }else{
                        this.url = "jsf://test-local-debug.jd.local:19999?"+this.url.substring(this.url.indexOf("?")+1);
                    }
                    System.out.println("[localDebug] replaceUrl "+beforeUrl +" to "+this.url);
                }else if(StringUtils.isNotEmpty(this.alias)){
                    this.setParameter("testInterfaceAndAlias",this.interfaceId+","+this.alias);
                    this.url = "jsf://test-local-debug.jd.local:19999?alias="+this.alias;
                    System.out.println("[localDebug] replaceUrl to "+this.url);
                }
            }



        }
    }
    public synchronized T refer() throws InitErrorException {
        preRefer();
        if (this.getSerialization() == null) {
            this.setSerialization("msgpack");
        }

        SecurityContext.initClientContext();
        if (this.proxyIns != null) {
            return this.proxyIns;
        } else {
            this.interfaceId = this.interfaceId.trim();
            this.alias = this.alias.trim();
            String key = this.buildKey();
            if (StringUtils.isBlank(this.alias)) {
                throw new InitErrorException("[JSF-21300]Value of \"alias\" value is not specified in consumer config with key " + key + " !");
            } else {
                this.getProxyClass();
                LOGGER.info("Refer consumer config : {} with bean id {}", key, this.getId());
                int c = JSFContext.incrementConsumerCount(key);
                if (c > 3) {
                    if (!CommonUtils.isFalse(this.getParameter(".warnning"))) {
                        throw new InitErrorException("[JSF-21304]Duplicate consumer config with key " + key + " has been referred more than 3 times! Maybe it's wrong config, please check it. Ignore this if you did that on purpose!");
                    }

                    LOGGER.warn("[JSF-21304]Duplicate consumer config with key {} has been referred more than 3 times! Maybe it's wrong config, please check it. Ignore this if you did that on purpose!", key);
                } else if (c > 1) {
                    LOGGER.warn("[JSF-21303]Duplicate consumer config with key {} has been referred! Maybe it's wrong config, please check it. Ignore this if you did that on purpose!", key);
                }

                CallbackUtil.autoRegisterCallBack(this.getProxyClass());
                final CountDownLatch registryDownLatch = new CountDownLatch(1);
                final AtomicReference<InitErrorException> exceptionReference = new AtomicReference();
                if (!this.isGeneric()) {
                    (new Thread(new Runnable() {
                        public void run() {
                            try {
                                CodecUtils.registryService(ConsumerConfig.this.serialization, new Class[]{ConsumerConfig.this.getProxyClass()});
                            } catch (InitErrorException var6) {
                                exceptionReference.set(var6);
                            } catch (Throwable var7) {
                                exceptionReference.set(new InitErrorException("[JSF-21305]Registry codec template error!", var7));
                            } finally {
                                registryDownLatch.countDown();
                            }

                        }
                    })).start();
                } else {
                    registryDownLatch.countDown();
                }

                if (this.isInjvm() && BaseServerHandler.getInvoker(this.getInterfaceId(), this.getAlias()) != null) {
                    LOGGER.info("Find matched provider invoker in current jvm, will invoke preferentially until it unexported");
                }

                this.configListener = new ConsumerConfig.ConsumerAttributeListener();
                this.providerListener = new ConsumerConfig.ClientProviderListener();

                try {
                    try {
                        this.client = ClientFactory.getClient(this);
                    } catch (NoRouterException var6) {
                        throw new InitErrorException(var6.getMessage(), var6);
                    }

                    this.proxyInvoker = new ClientProxyInvoker(this);
                    ProtocolFactory.check(ProtocolType.valueOf(this.getProtocol()), CodecType.valueOf(this.getSerialization()));
                    this.proxyIns = (T) ProxyFactory.buildProxy(this.getProxy(), this.getProxyClass(), this.proxyInvoker);
                    registryDownLatch.await();
                    InitErrorException initErrorException = (InitErrorException)exceptionReference.get();
                    if (initErrorException != null) {
                        throw initErrorException;
                    }
                } catch (Exception var7) {
                    if (this.client != null) {
                        this.client.destroy();
                        this.client = null;
                    }

                    JSFContext.decrementConsumerCount(key);
                    if (var7 instanceof InitErrorException) {
                        throw (InitErrorException)var7;
                    }

                    throw new InitErrorException("[JSF-21306]Build consumer proxy error!", var7);
                }

                if (this.onavailable != null && this.client != null) {
                    this.client.checkStateChange(false);
                }

                JSFContext.cacheConsumerConfig(this);
                return this.proxyIns;
            }
        }
    }

    public synchronized void unrefer() {
        if (this.proxyIns != null) {
            String key = this.buildKey();
            LOGGER.info("Unrefer consumer config : {} with bean id {}", key, this.getId());

            try {
                this.client.destroy();
            } catch (Exception var3) {
                LOGGER.warn("Catch exception when unrefer consumer config : " + key + ", but you can ignore if it's called by JVM shutdown hook", var3);
            }

            JSFContext.decrementConsumerCount(key);
            this.configListener = null;
            this.providerListener = null;
            JSFContext.invalidateConsumerConfig(this);
            RpcStatus.removeStatus(this);
            this.proxyIns = null;
            this.unsubscribe();
        }
    }

    public List<Provider> subscribe() {
        List<Provider> tmpProviderList = new ArrayList();
        Iterator var2 = this.getRegistry().iterator();

        while(var2.hasNext()) {
            RegistryConfig registryConfig = (RegistryConfig)var2.next();
            ClientRegistry registry = RegistryFactory.getRegistry(registryConfig);

            try {
                List<Provider> providers = registry.subscribe(this, this.providerListener, this.configListener);
                if (CommonUtils.isNotEmpty(providers)) {
                    tmpProviderList.addAll(providers);
                }
            } catch (InitErrorException var6) {
                throw var6;
            } catch (Exception var7) {
                LOGGER.warn("Catch exception when subscribe from registry: " + registryConfig.getId() + ", but you can ignore if it's called by JVM shutdown hook", var7);
            }
        }

        return tmpProviderList;
    }

    public void unsubscribe() {
        if (StringUtils.isEmpty(this.url) && this.isSubscribe()) {
            List<RegistryConfig> registryConfigs = super.getRegistry();
            if (registryConfigs != null) {
                Iterator var2 = registryConfigs.iterator();

                while(var2.hasNext()) {
                    RegistryConfig registryConfig = (RegistryConfig)var2.next();
                    ClientRegistry registry = RegistryFactory.getRegistry(registryConfig);

                    try {
                        registry.unsubscribe(this);
                    } catch (Exception var6) {
                        LOGGER.warn("Catch exception when unsubscribe from registry: " + registryConfig.getId() + ", but you can ignore if it's called by JVM shutdown hook", var6);
                    }
                }
            }
        }

    }

    public Client getClient() {
        return this.client;
    }

    private class ConsumerAttributeListener implements ConfigListener {
        private ConsumerAttributeListener() {
        }

        public void configChanged(Map newValue) {
            if (ConsumerConfig.this.client != null) {
                if (newValue == null) {
                    return;
                }

                Map<String, String> attrs = new HashMap(newValue);
                String interfaceId = (String)attrs.remove("interface");
                JSFContext.putInterfaceValIfChanged(interfaceId, attrs, "unit.metadata", "");
                if (newValue.containsKey("router.open") || newValue.containsKey("router.rule") || newValue.containsKey("unit.metadata")) {
                    ConsumerConfig.this.client.resetRouters();
                }
            }

        }

        public void providerAttrUpdated(Map newValue) {
        }

        public synchronized void consumerAttrUpdated(Map newValueMap) {
            Map<String, String> newValues = newValueMap;
            Map<String, String> oldValues = new HashMap();
            boolean rerefer = false;

            Iterator var5;
            Entry entryx;
            boolean changed;
            try {
                for(var5 = newValues.entrySet().iterator(); var5.hasNext(); rerefer = rerefer || changed) {
                    entryx = (Entry)var5.next();
                    String newValue = (String)entryx.getValue();
                    String oldValue = ConsumerConfig.this.queryAttribute((String)entryx.getKey());
                    changed = oldValue == null ? newValue != null : !oldValue.equals(newValue);
                    if (changed) {
                        oldValues.put((String) entryx.getKey(), oldValue);
                    }
                }
            } catch (Exception var12) {
                ConsumerConfig.LOGGER.error("Catch exception when consumer attribute comparing", var12);
                return;
            }

            if (rerefer) {
                ConsumerConfig.LOGGER.info("Rerefer consumer {}", ConsumerConfig.this.buildKey());

                Iterator var14;
                Entry entry;
                try {
                    ConsumerConfig.this.unsubscribe();
                    var5 = newValues.entrySet().iterator();

                    while(var5.hasNext()) {
                        entryx = (Entry)var5.next();
                        ConsumerConfig.this.updateAttribute((String)entryx.getKey(), (String)entryx.getValue(), true);
                    }
                } catch (Exception var11) {
                    ConsumerConfig.LOGGER.error("Catch exception when consumer attribute changed", var11);
                    var14 = oldValues.entrySet().iterator();

                    while(var14.hasNext()) {
                        entry = (Entry)var14.next();
                        ConsumerConfig.this.updateAttribute((String)entry.getKey(), (String)entry.getValue(), true);
                    }

                    ConsumerConfig.this.subscribe();
                    return;
                }

                try {
                    ConnStrategyConfig connStrategyConfig = ConsumerConfig.this.getConnStrategy();
                    if (null != connStrategyConfig) {
                        connStrategyConfig.setInitialSize(connStrategyConfig.getMaxActive());
                        ConsumerConfig.this.setConnStrategy(connStrategyConfig);
                    }

                    this.switchClient();
                } catch (Exception var10) {
                    ConsumerConfig.LOGGER.error("Catch exception when consumer refer after attribute changed", var10);
                    ConsumerConfig.this.unsubscribe();
                    var14 = oldValues.entrySet().iterator();

                    while(var14.hasNext()) {
                        entry = (Entry)var14.next();
                        ConsumerConfig.this.updateAttribute((String)entry.getKey(), (String)entry.getValue(), true);
                    }

                    ConsumerConfig.this.subscribe();
                }
            }

        }

        private void switchClient() throws Exception {
            Client newclient = null;

            Client oldClient;
            try {
                newclient = ClientFactory.getClient(ConsumerConfig.this);
                oldClient = ((ClientProxyInvoker)ConsumerConfig.this.proxyInvoker).setClient(newclient);
            } catch (Exception var5) {
                if (newclient != null) {
                    newclient.destroy();
                }

                throw var5;
            }

            try {
                ConsumerConfig.this.client = newclient;
                if (oldClient != null) {
                    oldClient.destroy();
                }
            } catch (Exception var4) {
                ConsumerConfig.LOGGER.warn("Catch exception when destroy");
            }

        }
    }

    private class ClientProviderListener implements ProviderListener {
        private ClientProviderListener() {
        }

        public void addProvider(List<Provider> providers) {
            if (ConsumerConfig.this.client != null) {
                boolean originalState = ConsumerConfig.this.client.isAvailable();
                ConsumerConfig.this.client.addProvider(providers);
                ConsumerConfig.this.client.checkStateChange(originalState);
            }

        }

        public void removeProvider(List<Provider> providers) {
            if (ConsumerConfig.this.client != null) {
                boolean originalState = ConsumerConfig.this.client.isAvailable();
                ConsumerConfig.this.client.removeProvider(providers);
                ConsumerConfig.this.client.checkStateChange(originalState);
            }

        }

        public void updateProvider(List<Provider> newProviders) {
            if (ConsumerConfig.this.client != null) {
                boolean originalState = ConsumerConfig.this.client.isAvailable();
                ConsumerConfig.this.client.updateProvider(newProviders);
                ConsumerConfig.this.client.checkStateChange(originalState);
            }

        }
    }
}

