package com.jd.workflow.soap.wsdl;

import com.jd.workflow.soap.classinfo.ClassGenerator;
import com.jd.workflow.soap.classinfo.ClassSourceCompiler;
import com.jd.workflow.soap.classinfo.model.ClassInfo;
import com.jd.workflow.soap.classinfo.model.FieldInfo;
import com.jd.workflow.soap.classloader.MemoryClassLoader;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.exception.WsdlGenerateException;
import com.jd.workflow.soap.utils.StringHelper;
import com.jd.workflow.soap.wsdl.param.Param;
import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.soap.SoapBindingConfiguration;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.catalog.CatalogXmlSchemaURIResolver;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.catalog.OASISCatalogManagerHelper;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.common.xmlschema.XmlSchemaUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.source.mime.MimeAttribute;
import org.apache.cxf.databinding.source.mime.MimeSerializer;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.ServiceModelSchemaValidator;
import org.apache.cxf.service.factory.SimpleMethodDispatcher;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.*;

import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * 参考org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean
 http接口转换为webservice接口需要哪些信息？
 1. namespaceName ，也就是java类的包名
 */
public class WsdlGenerator {
    public static final String ENDPOINT_CLASS = "endpoint.class";
    public static final String GENERIC_TYPE = "generic.type";
    public static final String RAW_CLASS = "rawclass";
    public static final String WSDL_CREATE_IMPORTS = "org.apache.cxf.wsdl.create.imports";
    public static final String WRAPPERGEN_NEEDED = "wrapper.gen.needed";
    public static final String EXTRA_CLASS = "extra.class";
    public static final String MODE_OUT = "messagepart.mode.out";
    public static final String MODE_INOUT = "messagepart.mode.inout";
    public static final String HOLDER = "messagepart.isholder";
    public static final String HEADER = "messagepart.isheader";
    public static final String ELEMENT_NAME = "messagepart.elementName";
    public static final String METHOD = "operation.method";
    public static final String FORCE_TYPES = "operation.force.types";
    public static final String METHOD_PARAM_ANNOTATIONS = "method.parameters.annotations";
    public static final String METHOD_ANNOTATIONS = "method.return.annotations";
    public static final String PARAM_ANNOTATION = "parameter.annotations";
    public static final String PUBLISHED_ENDPOINT_URL = "publishedEndpointUrl";
    public static final String AUTO_REWRITE_ADDRESS = "autoRewriteSoapAddress";
    public static final String AUTO_REWRITE_ADDRESS_ALL = "autoRewriteSoapAddressForAllServices";


    static final Logger LOG = LoggerFactory.getLogger(WsdlGenerator.class);
    ServiceInfo serviceInfo = null;
    WsdlModelInfo modelInfo;
    JAXBDataBinding jaxbDataBinding;
    MemoryClassLoader memoryClassLoader;
    private MethodDispatcher methodDispatcher = new SimpleMethodDispatcher();
    protected final Map<String, String> schemaLocationMapping = new HashMap<>();
    Bus bus = null;


    public WsdlGenerator(WsdlModelInfo modelInfo) {
        try{
            Map<Class<?>, Object> map = new HashMap<>();
            map.put(WSDLManager.class,new WSDLManagerImpl());
            Bus bus = new ExtensionManagerBus(map);
            this.modelInfo = modelInfo;
            jaxbDataBinding = new JAXBDataBinding();
            // 动态生成java类，方便做转换操作
            Collection<ClassInfo> allClass = modelInfo.getAllClass();
            Map<String, String> classMap = ClassGenerator.generateClass(allClass);
            this.bus = bus;
            memoryClassLoader = new ClassSourceCompiler().compile(classMap);
        }catch (Exception e){
            LOG.error("wsdl.err_init_wsdl_generator",e);
            WsdlGenerateException exception = new WsdlGenerateException("wsdl.err_init_wsdl",e);
            throw exception;
        }
    }

    public Definition buildWsdlDefinition() throws WSDLException {
        ServiceInfo serviceInfo = buildServiceInfo();
        this.serviceInfo = serviceInfo;
        ServiceWSDLBuilder builder =
                new ServiceWSDLBuilder(bus, serviceInfo);

        builder.setUseSchemaImports(false);

        // base file name is ignored if createSchemaImports == false!
        builder.setBaseFileName(serviceInfo.getName().getLocalPart());

        Definition def = builder.build(new HashMap<String, SchemaInfo>());
        return def;
    }

    public Document buildServiceInfo(Definition def
                          ) throws  WSDLException, BusException {
        Document doc = writeWSDLDocument(def);
       /* if (params.containsKey("wsdl")) {
            String wsdl = URLDecoder.decode(params.get("wsdl"), "utf-8");
            doc = writeWSDLDocument(def);
        } else if (params.get("xsd") != null) {
            String xsd = URLDecoder.decode(params.get("xsd"), "utf-8");
            doc = readXSDDocument(bus, xsd, smp, base);
            updateDoc(doc, base, mp, smp, message, xsd);
        }*/
        return doc;
    }
    protected Document readXSDDocument(
                                       String xsd,
                                       Map<String, SchemaReference> smp,
                                       String base) throws XMLStreamException {
        /*final Document doc;
        SchemaReference si = lookupSchemaReference(bus, xsd, smp, base);

        String uri = si.getReferencedSchema().getDocumentBaseURI();
        uri = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus),
                uri, si.getReferencedSchema().getDocumentBaseURI());
        if (uri == null) {
            uri = si.getReferencedSchema().getDocumentBaseURI();
        }
        ResourceManagerWSDLLocator rml = new ResourceManagerWSDLLocator(uri, bus);

        InputSource src = rml.getBaseInputSource();
        if (src.getByteStream() != null || src.getCharacterStream() != null) {
            doc = StaxUtils.read(src);
        } else { // last resort lets try for the referenced schema itself.
            // its not thread safe if we use the same document
            doc = StaxUtils.read(
                    new DOMSource(si.getReferencedSchema().getElement().getOwnerDocument()));
        }

        return doc;*/
        return null;
    }
    public Document writeWSDLDocument(
                                      Definition def /*,
                                      Message message,
                                      Map<String, Definition> mp,
                                      Map<String, SchemaReference> smp,
                                      String wsdl,
                                      String base,
                                      EndpointInfo endpointInfo*/) throws WSDLException, BusException {

        Document doc;
       // String epurl = base;
        WSDLManager wsdlManager = new WSDLManagerImpl();
        synchronized (def) {
            //writing a def is not threadsafe.  Sync on it to make sure
            //we don't get any ConcurrentModificationExceptions
            //epurl = getPublishableEndpointUrl(def, epurl, endpointInfo);

            WSDLWriter wsdlWriter = wsdlManager
                    .getWSDLFactory().newWSDLWriter();
            def.setExtensionRegistry(wsdlManager.getExtensionRegistry());
            doc = wsdlWriter.getDocument(def);
        }

        //updateDoc(doc, epurl, mp, smp, message, wsdl);
        return doc;
    }
    @Deprecated
    protected void updateDoc(Document doc,
                             String base,
                             Map<String, Definition> mp,
                             Map<String, SchemaReference> smp,
                             Message message,
                             String xsd,
                             String wsdl) {
        updateDoc(doc, base, mp, smp, message, xsd != null ? xsd : wsdl);
    }

    protected void updateDoc(Document doc,
                             String base,
                             Map<String, Definition> mp,
                             Map<String, SchemaReference> smp,
                             Message message,
                             String xsdWsdlPar) {


        try {
            List<Element> elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://www.w3.org/2001/XMLSchema", "import");
            for (Element el : elementList) {
                String sl = el.getAttribute("schemaLocation");
                sl = mapUri( base, smp, sl, xsdWsdlPar, doc.getDocumentURI());
                if (sl != null) {
                    el.setAttribute("schemaLocation", sl);
                }
            }

            elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://www.w3.org/2001/XMLSchema",
                    "include");
            for (Element el : elementList) {
                String sl = el.getAttribute("schemaLocation");
                sl = mapUri( base, smp, sl, xsdWsdlPar, doc.getDocumentURI());
                if (sl != null) {
                    el.setAttribute("schemaLocation", sl);
                }
            }
            elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://www.w3.org/2001/XMLSchema",
                    "redefine");
            for (Element el : elementList) {
                String sl = el.getAttribute("schemaLocation");
                sl = mapUri(base, smp, sl, xsdWsdlPar, doc.getDocumentURI());
                if (sl != null) {
                    el.setAttribute("schemaLocation", sl);
                }
            }
            elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://schemas.xmlsoap.org/wsdl/",
                    "import");
            for (Element el : elementList) {
                String sl = el.getAttribute("location");
                try {
                    sl = getLocationURI(sl, xsdWsdlPar);
                } catch (URISyntaxException e) {
                    //ignore
                }
                if (mp.containsKey(URLDecoder.decode(sl, "utf-8"))) {
                    el.setAttribute("location", base + "?wsdl=" + sl.replace(" ", "%20"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new StdException("COULD_NOT_PROVIDE_WSDL",e);
        }

        boolean rewriteAllSoapAddress = MessageUtils.getContextualBoolean(message, AUTO_REWRITE_ADDRESS_ALL, false);
        if (rewriteAllSoapAddress) {
            List<Element> portList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://schemas.xmlsoap.org/wsdl/",
                    "port");
            String basePath = (String) message.get("http.base.path");
            for (Element el : portList) {
                rewriteAddressProtocolHostPort(base, el, basePath, "http://schemas.xmlsoap.org/wsdl/soap/");
                rewriteAddressProtocolHostPort(base, el, basePath, "http://schemas.xmlsoap.org/wsdl/soap12/");
            }
        }
        if (MessageUtils.getContextualBoolean(message, AUTO_REWRITE_ADDRESS, true) || rewriteAllSoapAddress) {
            List<Element> serviceList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                    "http://schemas.xmlsoap.org/wsdl/",
                    "service");
            for (Element serviceEl : serviceList) {
                String serviceName = serviceEl.getAttribute("name");
                if (serviceName.equals(message.getExchange().getService().getName().getLocalPart())) {
                    List<Element> elementList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(),
                            "http://schemas.xmlsoap.org/wsdl/",
                            "port");
                    for (Element el : elementList) {
                        String name = el.getAttribute("name");
                        if (name.equals(message.getExchange().getEndpoint().getEndpointInfo()
                                .getName().getLocalPart())) {
                            rewriteAddress(base, el, "http://schemas.xmlsoap.org/wsdl/soap/");
                            rewriteAddress(base, el, "http://schemas.xmlsoap.org/wsdl/soap12/");
                        }
                    }
                }
            }
        }
        try {
            doc.setXmlStandalone(true);
        } catch (Exception ex) {
            //likely not DOM level 3
        }
    }
    protected String mapUri(String base, Map<String, SchemaReference> smp,
                            String loc, String xsd, String resolvedXsd)
            throws UnsupportedEncodingException {
        String key = loc;
        try {
            boolean absoluteLocUri = new URI(loc).isAbsolute();
            if (!absoluteLocUri && xsd != null) { // XSD request
                // resolve requested location with relative import path
                key = new URI(xsd).resolve(loc).toString();

                SchemaReference ref = smp.get(URLDecoder.decode(key, "utf-8"));
                if (ref == null) {
                    // if the result is not known, check if we can resolve it into something known
                    String resolved = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus), key, base);
                    if (resolved != null  && smp.containsKey(URLDecoder.decode(resolved, "utf-8"))) {
                        // if it is resolvable, we can use it
                        return base + "?xsd=" + key.replace(" ", "%20");
                    }
                }
            } else if (!absoluteLocUri && xsd == null) { // WSDL request
                key = new URI(".").resolve(loc).toString();
            }
        } catch (URISyntaxException e) {
            //ignore
        }
        SchemaReference ref = smp.get(URLDecoder.decode(key, "utf-8"));
        if (ref == null && resolvedXsd != null) {
            try {
                String key2 = new URI(resolvedXsd).resolve(loc).toString();
                SchemaReference ref2 = smp.get(URLDecoder.decode(key2, "utf-8"));
                if (ref2 == null) {
                    // if the result is not known, check if we can resolve it into something known
                    String resolved = resolveWithCatalogs(OASISCatalogManager.getCatalogManager(bus), key2, base);
                    if (resolved != null  && smp.containsKey(URLDecoder.decode(resolved, "utf-8"))) {
                        // if it is resolvable, we can use it
                        ref = smp.get(URLDecoder.decode(resolved, "utf-8"));
                    }
                } else {
                    ref = smp.get(URLDecoder.decode(key2, "utf-8"));
                }
            } catch (URISyntaxException e) {
                //ignore, ref can remain null
            }
            if (ref != null) {
                // we are able to map this, but for some reason the default key passed in cannot
                // be used for a direct lookup, we need to create a unique import key
                int count = 1;
                while (smp.containsKey("_import" + count + ".xsd")) {
                    count++;
                }
                key = "_import" + count + ".xsd";
                smp.put(key, ref);
            }
        }
        if (ref != null) {
            return base + "?xsd=" + key.replace(" ", "%20");
        }
        return null;
    }
    protected String resolveWithCatalogs(OASISCatalogManager catalogs,
                                         String start,
                                         String base) {
        try {
            return new OASISCatalogManagerHelper().resolve(catalogs, start, base);
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }
    protected void rewriteAddress(String base,
                                  Element el,
                                  String soapNS) {
        List<Element> sadEls = DOMUtils.findAllElementsByTagNameNS(el, soapNS, "address");
        for (Element soapAddress : sadEls) {
            soapAddress.setAttribute("location", base);
        }
    }
    private String getLocationURI(String startLoc, String docBase) throws URISyntaxException {

        if (!(new URI(startLoc).isAbsolute())) {
            if (StringUtils.isEmpty(docBase)) {
                startLoc = new URI(".").resolve(startLoc).toString();
            } else {
                startLoc = new URI(docBase).resolve(startLoc).toString();
            }
        }
        return startLoc;
    }
    protected void rewriteAddressProtocolHostPort(String base,
                                                  Element el,
                                                  String httpBasePathProp,
                                                  String soapNS) {
        List<Element> sadEls = DOMUtils.findAllElementsByTagNameNS(el, soapNS, "address");
        for (Element soapAddress : sadEls) {
            String location = soapAddress.getAttribute("location").trim();
            try {
                URI locUri = new URI(location);
                if (locUri.isAbsolute()) {
                    URL baseUrl = new URL(base);
                    StringBuilder sb = new StringBuilder(baseUrl.getProtocol());
                    sb.append("://").append(baseUrl.getHost());
                    int port = baseUrl.getPort();
                    if (port > 0) {
                        sb.append(':').append(port);
                    }
                    sb.append(locUri.getPath());
                    soapAddress.setAttribute("location", sb.toString());
                } else if (httpBasePathProp != null) {
                    soapAddress.setAttribute("location", httpBasePathProp + location);
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }
    public String getPublishableEndpointUrl(Definition def,
                                            String epurl,
                                            EndpointInfo endpointInfo) {

        if (endpointInfo.getProperty(PUBLISHED_ENDPOINT_URL) != null) {
            epurl = String.valueOf(
                    endpointInfo.getProperty(PUBLISHED_ENDPOINT_URL));
            //updatePublishedEndpointUrl(epurl, def, endpointInfo.getName());
        }
        return epurl;
    }
    public DataBinding getDataBinding(){
        return this.jaxbDataBinding;
    }

    /**
     * 将java类转换为schema文档
     * @param service
     */
    protected void initializeDataBindings(Service service) {
        /*if (getDataBinding() instanceof AbstractDataBinding && schemaLocations != null) {
            fillDataBindingSchemas();
        }*/
        getDataBinding().initialize(service);

        service.setDataBinding(getDataBinding());
    }
    public boolean hasWrappedMethods(InterfaceInfo interfaceInfo) {
        for (OperationInfo opInfo : interfaceInfo.getOperations()) {
            if (opInfo.isUnwrappedCapable()) {
                return true;
            }
        }
        return false;
    }
    /*protected void setServiceProperties() {
        MethodDispatcher md = getMethodDispatcher();
        getService().put(MethodDispatcher.class.getName(), md);
        for (Class<?> c : md.getClass().getInterfaces()) {
            getService().put(c.getName(), md);
        }
        if (properties != null) {
            getService().putAll(properties);
        }
    }*/
  /*  public QName getServiceQName() {
        return getServiceQName(true);
    }*/
  /*  public QName getServiceQName(boolean lookup) {
        if (serviceName == null && lookup) {
            serviceName = new QName(getServiceNamespace(), getServiceName());
        }

        return serviceName;
    }
    public String getServiceName() {
        QName service = implInfo.getServiceName();
        return service.getLocalPart();
    }*/
    public ServiceInfo buildServiceInfo(){

        ServiceInfo serviceInfo = new ServiceInfo();
        Service service = new ServiceImpl(serviceInfo);
        SchemaCollection col = serviceInfo.getXmlSchemaCollection();
        col.getXmlSchemaCollection().setSchemaResolver(new CatalogXmlSchemaURIResolver(bus));
        col.getExtReg().registerSerializer(MimeAttribute.class, new MimeSerializer());

        //setService(service);
        //setServiceProperties();

        serviceInfo.setName(new QName(modelInfo.targetNamespace,modelInfo.getServiceName()));
        serviceInfo.setTargetNamespace(serviceInfo.getName().getNamespaceURI());


        createInterface(serviceInfo);

        Set<?> wrapperClasses = new HashSet<>(memoryClassLoader.getClasses().values());

            if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
                serviceInfo.setProperty(EXTRA_CLASS, wrapperClasses);
            }

        initializeDataBindings(service);
        boolean isWrapped = isWrapped()  ||   hasWrappedMethods(serviceInfo.getInterface());
        if (isWrapped) {
            initializeWrappedSchema(serviceInfo);
        }

        for (OperationInfo opInfo : serviceInfo.getInterface().getOperations()) {
            ServiceMethodInfo m = (ServiceMethodInfo)opInfo.getProperty(METHOD);
            if (!isWrapped(m) && !m.isRpc() && opInfo.getInput() != null) {
                createBareMessage(serviceInfo, opInfo, false);
            }

            if ( !isWrapped(m) && !m.isRpc() && opInfo.getOutput() != null) {
                createBareMessage(serviceInfo, opInfo, true);
            }

            if (opInfo.hasFaults()) {
                // check to make sure the faults are elements
                for (FaultInfo fault : opInfo.getFaults()) {
                    QName qn = (QName)fault.getProperty("elementName");
                    MessagePartInfo part = fault.getFirstMessagePart();
                    if (!part.isElement()) {
                        part.setElement(true);
                        part.setElementQName(qn);
                        checkForElement(serviceInfo, part);
                    }
                }
            }
        }
        createEndpointInfo(service,modelInfo.getEndpointUrl());

        if ( true) { //isValidate()
            ServiceModelSchemaValidator validator = new ServiceModelSchemaValidator(serviceInfo);
            validator.walk();
            String validationComplaints = validator.getComplaints();
            if (!"".equals(validationComplaints)) {
                LOG.warn(validationComplaints);

            }
        }


        return serviceInfo;
    }
    protected SoapBindingConfiguration createSoapBindingConfig(String transportId) {
        SoapBindingConfiguration bc
                = new SoapBindingConfiguration();
        if (transportId != null) {
            bc.setTransportURI(transportId);
        }
        return bc;
    }
    protected BindingInfo superCreateBindingInfo(String bindingId,Service service,String transportId) {
        //BindingFactoryManager mgr = bus.getExtension(BindingFactoryManager.class);
        String binding = bindingId;

        SoapBindingConfiguration bindingConfig = null;

        //try {
            if (binding.contains("/soap")) {
                if (bindingConfig == null) {
                    bindingConfig = createSoapBindingConfig(transportId);
                }
                if (bindingConfig instanceof SoapBindingConfiguration
                        && !((SoapBindingConfiguration)bindingConfig).isSetStyle()) {
                    ((SoapBindingConfiguration)bindingConfig).setStyle("document");
                }
            }

            BindingFactory bindingFactory = new SoapBindingFactory(bus);//mgr.getBindingFactory(binding);

            BindingInfo inf = bindingFactory.createBindingInfo(service,
                    binding, bindingConfig);

          /*  for (BindingOperationInfo boi : inf.getOperations()) {
                serviceFactory.updateBindingOperation(boi);
                Method m = serviceFactory.getMethodDispatcher().getMethod(boi);
                serviceFactory.sendEvent(FactoryBeanListener.Event.BINDING_OPERATION_CREATED, inf, boi, m);
            }
            serviceFactory.sendEvent(FactoryBeanListener.Event.BINDING_CREATED, inf);*/
            return inf;
        /*} catch (BusException ex) {
            throw new StdException("COULD.NOT.RESOLVE.BINDING",ex);
        }*/
    }
    protected BindingInfo createBindingInfo(Service service,String transportId) {
      /*  JaxWsServiceFactoryBean sf = (JaxWsServiceFactoryBean)getServiceFactory();

        JaxWsImplementorInfo implInfo = sf.getJaxWsImplementorInfo();
        String jaxBid = implInfo.getBindingType();*/
        String binding = SOAPBinding.SOAP11HTTP_BINDING;


        if (binding.equals(SOAPBinding.SOAP11HTTP_BINDING)
                || binding.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
            binding = "http://schemas.xmlsoap.org/wsdl/soap/";

        } else if (binding.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            binding = SOAPBinding.SOAP12HTTP_BINDING;

        }


        BindingInfo bindingInfo = superCreateBindingInfo(binding,service,transportId);

        /*if (implInfo.isWebServiceProvider()) {
            bindingInfo.getService().setProperty("soap.force.doclit.bare", Boolean.TRUE);
            if (this.getServiceFactory().isPopulateFromClass()) {
                for (EndpointInfo ei : bindingInfo.getService().getEndpoints()) {
                    ei.setProperty("soap.no.validate.parts", Boolean.TRUE);
                }
                //Provider, but no wsdl.  Synthetic ops
                for (BindingOperationInfo op : bindingInfo.getOperations()) {
                    op.setProperty("operation.is.synthetic", Boolean.TRUE);
                    op.getOperationInfo().setProperty("operation.is.synthetic", Boolean.TRUE);
                }
            }
        }*/

        return bindingInfo;
    }

    public EndpointInfo createEndpointInfo(Service service,String address){

        // setup the transport ID for the soap over jms if there is only address information
       /* if (transportId == null && getAddress() != null
                && getAddress().startsWith("jms:") && !"jms://".equals(getAddress())) {
            // Set the transportId to be soap over jms transport
            transportId = SoapJMSConstants.SOAP_JMS_SPECIFICIATION_TRANSPORTID;
        }*/
        QName endpointName = new QName(modelInfo.getTargetNamespace(),modelInfo.getServiceName()+"Port");
        String transportId = null;
        BindingInfo bindingInfo = null;
        if (transportId == null) {
            if (bindingInfo instanceof SoapBindingInfo) {
                transportId = ((SoapBindingInfo)bindingInfo).getTransportURI();
            }
           /* if (transportId == null
                    && getAddress() != null
                    && getAddress().contains("://")) {
                transportId = detectTransportIdFromAddress(getAddress());
            }*/
            if (transportId == null) {
                transportId = "http://schemas.xmlsoap.org/soap/http";
            }
        }
        // Get the Service from the ServiceFactory if specified
        if (bindingInfo == null) {
            // SOAP nonsense
            bindingInfo = createBindingInfo(service,transportId);
          /*  if (bindingInfo instanceof SoapBindingInfo
                    && (((SoapBindingInfo) bindingInfo).getTransportURI() == null
                    || LocalTransportFactory.TRANSPORT_ID.equals(transportId))) {
                ((SoapBindingInfo) bindingInfo).setTransportURI(transportId);
                transportId = "http://schemas.xmlsoap.org/wsdl/soap/";
            }*/
            service.getServiceInfos().get(0).addBinding(bindingInfo);
        }


        //setTransportId(transportId);

        WSDLEndpointFactory wsdlEndpointFactory = getWSDLEndpointFactory();
        EndpointInfo ei;
        if (wsdlEndpointFactory != null) {
            ei = wsdlEndpointFactory.createEndpointInfo(bus, service.getServiceInfos().get(0), bindingInfo, null);
            ei.setTransportId(transportId);
        } else {
            ei = new EndpointInfo(service.getServiceInfos().get(0), transportId);
        }
        int count = 1;
        while (service.getEndpointInfo(endpointName) != null) {
            endpointName = new QName(endpointName.getNamespaceURI(),
                    endpointName.getLocalPart() + count);
            count++;
        }
        ei.setName(endpointName);
        ei.setAddress(address);
        ei.setBinding(bindingInfo);

        if (wsdlEndpointFactory != null) {
            wsdlEndpointFactory.createPortExtensors(bus, ei, service);
        }
        service.getServiceInfos().get(0).addEndpoint(ei);

        return ei;
    }
    protected WSDLEndpointFactory getWSDLEndpointFactory() {
        return new SoapTransportFactory();
    }
    protected void initializeWrappedSchema(ServiceInfo serviceInfo) {
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.getUnwrappedOperation() != null) {
                if (op.hasInput()) {
                    MessagePartInfo fmpi = op.getInput().getFirstMessagePart();
                    if (fmpi.getTypeClass() == null) {

                        QName wrapperBeanName = fmpi.getElementQName();
                        XmlSchemaElement e = null;
                        for (SchemaInfo s : serviceInfo.getSchemas()) {
                            e = s.getElementByQName(wrapperBeanName);
                            if (e != null) {
                                fmpi.setXmlSchema(e);
                                break;
                            }
                        }
                        if (e == null) {
                            createWrappedSchema(serviceInfo, op.getInput(), op.getUnwrappedOperation()
                                    .getInput(), wrapperBeanName);
                        }
                    }

                    for (MessagePartInfo mpi : op.getInput().getMessageParts()) {
                        if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                            QName qn = (QName)mpi.getProperty(ELEMENT_NAME);
                            mpi.setElement(true);
                            mpi.setElementQName(qn);

                            checkForElement(serviceInfo, mpi);
                        }
                    }

                }
                if (op.hasOutput()) {
                    MessagePartInfo fmpi = op.getOutput().getFirstMessagePart();
                    if (fmpi.getTypeClass() == null) {

                        QName wrapperBeanName = fmpi.getElementQName();
                        XmlSchemaElement e = null;
                        for (SchemaInfo s : serviceInfo.getSchemas()) {
                            e = s.getElementByQName(wrapperBeanName);
                            if (e != null) {
                                break;
                            }
                        }
                        if (e == null) {
                            createWrappedSchema(serviceInfo, op.getOutput(), op.getUnwrappedOperation()
                                    .getOutput(), wrapperBeanName);
                        }
                    }
                    for (MessagePartInfo mpi : op.getOutput().getMessageParts()) {
                        if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                            QName qn = (QName)mpi.getProperty(ELEMENT_NAME);
                            mpi.setElement(true);
                            mpi.setElementQName(qn);

                            checkForElement(serviceInfo, mpi);
                        }
                    }
                }
            }
        }

    }
    protected void createWrappedSchema(ServiceInfo serviceInfo, AbstractMessageContainer wrappedMessage,
                                       AbstractMessageContainer unwrappedMessage, QName wrapperBeanName) {
        SchemaInfo schemaInfo = getOrCreateSchema(serviceInfo, wrapperBeanName.getNamespaceURI(),
                getQualifyWrapperSchema());

        createWrappedMessageSchema(serviceInfo, wrappedMessage, unwrappedMessage, schemaInfo,
                wrapperBeanName);
    }
    private void createWrappedMessageSchema(ServiceInfo serviceInfo, AbstractMessageContainer wrappedMessage,
                                            AbstractMessageContainer unwrappedMessage, SchemaInfo info,
                                            QName wrapperName) {

        XmlSchema schema = info.getSchema();
        info.setElement(null); // the cached schema will be no good
        XmlSchemaElement el = new XmlSchemaElement(schema, true);
        el.setName(wrapperName.getLocalPart());

        wrappedMessage.getFirstMessagePart().setXmlSchema(el);

        boolean anonymousType = false;//isAnonymousWrapperTypes();
        XmlSchemaComplexType ct = new XmlSchemaComplexType(schema,
                /*CXF-6783: don't create anonymous top-level types*/!anonymousType);

        if (!anonymousType) {
            ct.setName(wrapperName.getLocalPart());
            el.setSchemaTypeName(wrapperName);
        }
        el.setSchemaType(ct);

        XmlSchemaSequence seq = new XmlSchemaSequence();
        ct.setParticle(seq);

        for (MessagePartInfo mpi : unwrappedMessage.getMessageParts()) {
            el = new XmlSchemaElement(schema, Boolean.TRUE.equals(mpi.getProperty(HEADER)));
            Map<Class<?>, Boolean> jaxbAnnoMap = new HashMap<>();// getJaxbAnnoMap(mpi);
            if (mpi.isElement()) {
                addImport(schema, mpi.getElementQName().getNamespaceURI());
                XmlSchemaUtils.setElementRefName(el, mpi.getElementQName());
            } else {
                // We hope that we can't have parts that different only in namespace.
                el.setName(mpi.getName().getLocalPart());
                if (mpi.getTypeQName() != null && !jaxbAnnoMap.containsKey(XmlList.class)) {
                    el.setSchemaTypeName(mpi.getTypeQName());
                    addImport(schema, mpi.getTypeQName().getNamespaceURI());
                }

                el.setSchemaType((XmlSchemaType)mpi.getXmlSchema());

                if (schema.getElementFormDefault().equals(XmlSchemaForm.UNQUALIFIED)) {
                    mpi.setConcreteName(new QName(null, mpi.getName().getLocalPart()));
                } else {
                    mpi.setConcreteName(mpi.getName());
                }
            }
            if (!Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                boolean wasType = !mpi.isElement();
                if (wasType) {
                    QName concreteName = mpi.getConcreteName();
                    mpi.setElement(true);
                    mpi.setElementQName(el.getQName());
                    mpi.setConcreteName(concreteName);
                }

                /*addMimeType(el, getMethodParameterAnnotations(mpi));
                Annotation[] methodAnnotations = getMethodAnnotations(mpi);
                if (methodAnnotations != null) {
                    addMimeType(el, methodAnnotations);
                }*/

                long min =1; //getWrapperPartMinOccurs(mpi);
                long max = 1;//getWrapperPartMaxOccurs(mpi);
                boolean nillable =  mpi.getProperty("nillable") != null ? (Boolean)mpi.getProperty("nillable") :false;;
                Boolean qualified = false;//isWrapperPartQualified(mpi);
                /*if (qualified == null) {
                    qualified = this.isQualifyWrapperSchema();
                }*/
                if (qualified
                        && StringUtils.isEmpty(mpi.getConcreteName().getNamespaceURI())) {
                    QName newName = new QName(wrapperName.getNamespaceURI(),
                            mpi.getConcreteName().getLocalPart());
                    mpi.setElement(true);
                    mpi.setElementQName(newName);
                    mpi.setConcreteName(newName);
                    el.setName(newName.getLocalPart());
                    el.setForm(XmlSchemaForm.QUALIFIED);
                }

                if (Collection.class.isAssignableFrom(mpi.getTypeClass())
                        && mpi.getTypeClass().isInterface()) {
                    Type type = (Type)mpi.getProperty(GENERIC_TYPE);

                    if (!(type instanceof java.lang.reflect.ParameterizedType)
                            && el.getSchemaTypeName() == null && el.getSchemaType() == null) {
                        max = Long.MAX_VALUE;
                        el.setSchemaTypeName(Constants.XSD_ANYTYPE);
                    }
                }
                el.setMinOccurs(min);
                el.setMaxOccurs(max);
                if (nillable) {
                    el.setNillable(nillable);
                }
                seq.getItems().add(el);
                mpi.setXmlSchema(el);
            }
            if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                QName qn = (QName)mpi.getProperty(ELEMENT_NAME);
                el.setName(qn.getLocalPart());

                SchemaInfo headerSchemaInfo = getOrCreateSchema(serviceInfo, qn.getNamespaceURI(),
                        getQualifyWrapperSchema());
                if (!isExistSchemaElement(headerSchemaInfo.getSchema(), qn)) {
                    headerSchemaInfo.getSchema().getItems().add(el);
                }
            }
        }

    }
    private XmlSchemaElement getExistingSchemaElement(XmlSchema schema, QName qn) {
        return schema.getElements().get(qn);
    }

    private boolean isExistSchemaElement(XmlSchema schema, QName qn) {
        return getExistingSchemaElement(schema, qn) != null;
    }


    private boolean isWrapped() {
        return true;
    }

    public final boolean getQualifyWrapperSchema() {
        return true;
    }
    protected void checkForElement(ServiceInfo serviceInfo, MessagePartInfo mpi) {
        SchemaInfo si = getOrCreateSchema(serviceInfo, mpi.getElementQName().getNamespaceURI(),
                getQualifyWrapperSchema());
        XmlSchemaElement e = si.getSchema().getElementByName(mpi.getElementQName().getLocalPart());
        if (e != null) {
            mpi.setXmlSchema(e);
            return;
        }
        XmlSchema schema = si.getSchema();
        si.setElement(null); //cached element is now invalid

        XmlSchemaElement el = new XmlSchemaElement(schema, true);
        el.setName(mpi.getElementQName().getLocalPart());
        el.setNillable(true);

        XmlSchemaType tp = (XmlSchemaType)mpi.getXmlSchema();
        if (tp == null) {
            throw new StdException("build_fail:INTRACTABLE_PART");

        }
        el.setSchemaTypeName(tp.getQName());
        mpi.setXmlSchema(el);
    }
    private SchemaInfo getOrCreateSchema(ServiceInfo serviceInfo, String namespaceURI, boolean qualified) {
        for (SchemaInfo s : serviceInfo.getSchemas()) {
            if (s.getNamespaceURI().equals(namespaceURI)) {
                return s;
            }
        }

        SchemaInfo schemaInfo = new SchemaInfo(namespaceURI);
        SchemaCollection col = serviceInfo.getXmlSchemaCollection();
        XmlSchema schema = col.getSchemaByTargetNamespace(namespaceURI);

        if (schema != null) {
            schemaInfo.setSchema(schema);
            serviceInfo.addSchema(schemaInfo);
            return schemaInfo;
        }

        schema = col.newXmlSchemaInCollection(namespaceURI);
        if (qualified) {
            schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        }
        schemaInfo.setSchema(schema);

        Map<String, String> explicitNamespaceMappings = this.getDataBinding().getDeclaredNamespaceMappings();
        if (explicitNamespaceMappings == null) {
            explicitNamespaceMappings = Collections.emptyMap();
        }
        NamespaceMap nsMap = new NamespaceMap();
        for (Map.Entry<String, String> mapping : explicitNamespaceMappings.entrySet()) {
            nsMap.add(mapping.getValue(), mapping.getKey());
        }

        if (!explicitNamespaceMappings.containsKey(WSDLConstants.NS_SCHEMA_XSD)) {
            nsMap.add(WSDLConstants.NP_SCHEMA_XSD, WSDLConstants.NS_SCHEMA_XSD);
        }
        if (!explicitNamespaceMappings.containsKey(serviceInfo.getTargetNamespace())) {
            nsMap.add(WSDLConstants.CONVENTIONAL_TNS_PREFIX, serviceInfo.getTargetNamespace());
        }
        schema.setNamespaceContext(nsMap);
        serviceInfo.addSchema(schemaInfo);
        return schemaInfo;
    }
    protected boolean isOutParam(ServiceMethodInfo method, int j) {
        return false;
    }
    protected void createBareMessage(ServiceInfo serviceInfo, OperationInfo opInfo, boolean isOut) {

        MessageInfo message = isOut ? opInfo.getOutput() : opInfo.getInput();

        final List<MessagePartInfo> messageParts = message.getMessageParts();
        if (messageParts.isEmpty()) {
            return;
        }

        ServiceMethodInfo method = (ServiceMethodInfo)opInfo.getProperty(METHOD);
        int paraNumber = 0;
        for (MessagePartInfo mpi : messageParts) {
            SchemaInfo schemaInfo = null;

            QName qname = (QName)mpi.getProperty(ELEMENT_NAME);
            if (messageParts.size() == 1 && qname == null) {
                qname = !isOut ? getInParameterName(opInfo, method, -1)
                        : getOutParameterName(opInfo, method, -1);

                if (qname.getLocalPart().startsWith("arg") || qname.getLocalPart().startsWith("return")) {
                    qname = isOut
                            ? new QName(qname.getNamespaceURI(), method.getName() + "Response") : new QName(qname
                            .getNamespaceURI(), method.getName());
                }
            } else if (isOut && messageParts.size() > 1 && qname == null) {
                while (!isOutParam(method, paraNumber)) {
                    paraNumber++;
                }
                qname = getOutParameterName(opInfo, method, paraNumber);
            } else if (qname == null) {
                qname = getInParameterName(opInfo, method, paraNumber);
            }

            for (SchemaInfo s : serviceInfo.getSchemas()) {
                if (s.getNamespaceURI().equals(qname.getNamespaceURI())) {
                    schemaInfo = s;
                    break;
                }
            }

            final XmlSchema schema;
            if (schemaInfo == null) {
                schemaInfo = getOrCreateSchema(serviceInfo, qname.getNamespaceURI(), true);
                schema = schemaInfo.getSchema();
            } else {
                schema = schemaInfo.getSchema();
                if (schema != null && schema.getElementByName(qname) != null) {
                    mpi.setElement(true);
                    mpi.setElementQName(qname);
                    mpi.setXmlSchema(schema.getElementByName(qname));
                    paraNumber++;
                    continue;
                }
            }

            schemaInfo.setElement(null); //cached element is now invalid
            XmlSchemaElement el = new XmlSchemaElement(schema, true);
            el.setName(qname.getLocalPart());
            el.setNillable(true);

            if (mpi.isElement()) {
                XmlSchemaElement oldEl = (XmlSchemaElement)mpi.getXmlSchema();
                if (null != oldEl && !oldEl.getQName().equals(qname)) {
                    el.setSchemaTypeName(oldEl.getSchemaTypeName());
                    el.setSchemaType(oldEl.getSchemaType());
                    if (oldEl.getSchemaTypeName() != null) {
                        addImport(schema, oldEl.getSchemaTypeName().getNamespaceURI());
                    }
                }
                mpi.setElement(true);
                mpi.setXmlSchema(el);
                mpi.setElementQName(qname);
                mpi.setConcreteName(qname);
                continue;
            }
            if (null == mpi.getTypeQName() && mpi.getXmlSchema() == null) {
                throw new StdException("build_msg_fail");
            }
            if (mpi.getTypeQName() != null) {
                el.setSchemaTypeName(mpi.getTypeQName());
            } else {
                el.setSchemaType((XmlSchemaType)mpi.getXmlSchema());
            }
            mpi.setXmlSchema(el);
            mpi.setConcreteName(qname);
            if (mpi.getTypeQName() != null) {
                addImport(schema, mpi.getTypeQName().getNamespaceURI());
            }

            mpi.setElement(true);
            mpi.setElementQName(qname);
            paraNumber++;
        }
    }
    private void addImport(XmlSchema schema, String ns) {
        if (!ns.equals(schema.getTargetNamespace())
                && !ns.equals(WSDLConstants.NS_SCHEMA_XSD)
                && !isExistImport(schema, ns)) {
            XmlSchemaImport is = new XmlSchemaImport(schema);
            is.setNamespace(ns);
            if (this.schemaLocationMapping.get(ns) != null) {
                is.setSchemaLocation(this.schemaLocationMapping.get(ns));
            }
            if (!schema.getItems().contains(is)) {
                schema.getItems().add(is);
            }
        }
    }
    private boolean isExistImport(XmlSchema schema, String ns) {
        boolean isExist = false;

        for (XmlSchemaExternal ext : schema.getExternals()) {
            if (ext instanceof XmlSchemaImport) {
                XmlSchemaImport xsImport = (XmlSchemaImport)ext;
                if (xsImport.getNamespace().equals(ns)) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;

    }
    private QName getInterfaceName(){
        return new QName(modelInfo.getTargetNamespace(),modelInfo.getServiceName()+"PortType");

    }
    protected InterfaceInfo createInterface(ServiceInfo serviceInfo) {
        QName intfName = getInterfaceName();
        InterfaceInfo intf = new InterfaceInfo(serviceInfo, intfName);

        List<ServiceMethodInfo> methods = modelInfo.getMethods();

        // The BP profile states we can't have operations of the same name
        // so we have to append numbers to the name. Different JVMs sort methods
        // differently.
        // We need to keep them ordered so if we have overloaded methods, the
        // wsdl is generated the same every time across JVMs and across
        // client/servers.

        Collections.sort(methods);

        for (ServiceMethodInfo m : methods) {

                createOperation(serviceInfo, intf, m);

        }
        //sendEvent(FactoryBeanListener.Event.INTERFACE_CREATED, intf, getServiceClass());
        return intf;
    }

    private OperationInfo createOperation(ServiceInfo serviceInfo, InterfaceInfo intf, ServiceMethodInfo m) {
        OperationInfo op = intf.addOperation(getOperationName(intf, m));
        // op.setProperty(m.getClass().getName(), m);
        op.setProperty("action", getAction(op, m));
        boolean isrpc = m.isRpc();
        if (!isrpc && isWrapped(m)) {//
            UnwrappedOperationInfo uOp = new UnwrappedOperationInfo(op);
            //uOp.setProperty(METHOD_ANNOTATIONS, annotations);
            //uOp.setProperty(METHOD_PARAM_ANNOTATIONS, parAnnotations);
            op.setUnwrappedOperation(uOp);

            createMessageParts(intf, uOp, m);

            if (uOp.hasInput()) {
                MessageInfo msg = new MessageInfo(op, MessageInfo.Type.INPUT, uOp.getInput().getName());
                op.setInput(uOp.getInputName(), msg);

                createInputWrappedMessageParts(uOp, m, msg);

                for (MessagePartInfo p : uOp.getInput().getMessageParts()) {
                    p.setConcreteName(p.getName());
                }
            }

            if (uOp.hasOutput()) {

                QName name = uOp.getOutput().getName();
                MessageInfo msg = new MessageInfo(op, MessageInfo.Type.OUTPUT, name);
                op.setOutput(uOp.getOutputName(), msg);

                createOutputWrappedMessageParts(uOp, m, msg);

                for (MessagePartInfo p : uOp.getOutput().getMessageParts()) {
                    p.setConcreteName(p.getName());
                }
            }
        } else {
            if (isrpc) {
                op.setProperty(FORCE_TYPES, Boolean.TRUE);
            }
            createMessageParts(intf, op, m);
        }

        //bindOperation(op, m);

        //sendEvent(FactoryBeanListener.Event.INTERFACE_OPERATION_BOUND, op, m);
        return op;
    }

    /**
     *  isWrapped不同含义的区别：
     *  http://cn.voidcc.com/question/p-hgefyake-bcn.html
     * @param m
     * @return
     */
    private boolean isWrapped(ServiceMethodInfo m) {
        return true;
    }

    /* public MethodDispatcher getMethodDispatcher() {
         return methodDispatcher;
     }
     protected void bindOperation(OperationInfo op, Method m) {
         getMethodDispatcher().bind(op, m);
     }*/
    protected void createOutputWrappedMessageParts(OperationInfo op, ServiceMethodInfo method, MessageInfo outMsg) {
        String partName = null;
        /*for (AbstractServiceConfiguration c : serviceConfigurations) {
            partName = c.getResponseWrapperPartName(op, method);
            if (partName != null) {
                break;
            }
        }*/
        for (MessagePartInfo mpart : op.getOutput().getMessageParts()) {
            if (Boolean.TRUE.equals(mpart.getProperty(HEADER))) {
                partName = "result";
                break;
            }
        }

        if (partName == null) {
            partName = "parameters";
        }

        MessagePartInfo part = outMsg.addMessagePart(partName);
        part.setElement(true);
        part.setIndex(0);
      /*  for (AbstractServiceConfiguration c : serviceConfigurations) {
            QName q = c.getResponseWrapperName(op, method);
            if (q != null) {
                part.setElementQName(q);
                break;
            }
        }*/

        if (part.getElementQName() == null) {
            part.setElementQName(outMsg.getName());
        } else if (!part.getElementQName().equals(op.getOutput().getName())) {
            op.getOutput().setName(part.getElementQName());
        }

    /*    if (this.getResponseWrapper(method) != null) {
            part.setTypeClass(this.getResponseWrapper(method));
        } else if (getResponseWrapperClassName(method) != null) {
            part.setProperty("RESPONSE.WRAPPER.CLASSNAME", getResponseWrapperClassName(method));
        }*/

        for (MessagePartInfo mpart : op.getOutput().getMessageParts()) {
            if (Boolean.TRUE.equals(mpart.getProperty(HEADER))) {
                int idx = mpart.getIndex();
                outMsg.addMessagePart(mpart);
                mpart.setIndex(idx);
            }
        }
    }

    /**
     * 将operation里的多个part参数合并为一个parameters参数，
     * @param op
     * @param method
     * @param inMsg
     */
    protected void createInputWrappedMessageParts(OperationInfo op, ServiceMethodInfo method, MessageInfo inMsg) {
        String partName = "parameters";

        MessagePartInfo part = inMsg.addMessagePart(partName);
        part.setElement(true);
       /* for (AbstractServiceConfiguration c : serviceConfigurations) {
            QName q = c.getRequestWrapperName(op, method);
            if (q != null) {
                part.setElementQName(q);
            }
        }*/
        if (part.getElementQName() == null) {
            part.setElementQName(inMsg.getName());
        } else if (!part.getElementQName().equals(op.getInput().getName())) {
            op.getInput().setName(part.getElementQName());
        }
       /* if (getRequestWrapper(method) != null) {
            part.setTypeClass(this.getRequestWrapper(method));
        } else if (getRequestWrapperClassName(method) != null) {
            part.setProperty("REQUEST.WRAPPER.CLASSNAME", getRequestWrapperClassName(method));
        }*/

        int partIdx = 0;
        int maxIdx = 0;
        for (MessagePartInfo mpart : op.getInput().getMessageParts()) {
            if (Boolean.TRUE.equals(mpart.getProperty(HEADER))) {
                int idx = mpart.getIndex();
                inMsg.addMessagePart(mpart);
                mpart.setIndex(idx);

                //make sure the header part and the wrapper part don't share the
                //same index.   We can move the wrapper part around a bit
                //if need be
                if (maxIdx < idx) {
                    maxIdx = idx;
                }
                if (idx == partIdx) {
                    maxIdx++;
                    partIdx = maxIdx;
                }
            }
        }
        part.setIndex(partIdx);

    }
    // CHECKSTYLE:OFF
    protected void createMessageParts(InterfaceInfo intf, OperationInfo op, ServiceMethodInfo method) {
        // Setup the input message
        //op.setProperty(METHOD, method);
        MessageInfo inMsg = op.createMessage(new QName(modelInfo.getTargetNamespace(),method.getName()), MessageInfo.Type.INPUT);
        op.setInput(inMsg.getName().getLocalPart(), inMsg);
        //final Annotation[][] parAnnotations = method.getParameterAnnotations();
        //final Type[] genParTypes = method.getGenericParameterTypes();
        for (int j = 0; j < method.getInputParams().size(); j++) {


                QName q = getInParameterName(op, method, j);
                QName partName = getInPartName(op, method, j);
                if (!method.isRpc() && !isWrapped(method)
                        && inMsg.getMessagePartsMap().containsKey(partName)) {
                    //LOG.log(Level.WARNING, "INVALID_BARE_METHOD", getServiceClass() + "." + method.getName());
                    partName = new QName(partName.getNamespaceURI(), partName.getLocalPart() + j);
                    q = new QName(q.getNamespaceURI(), q.getLocalPart() + j);
                }
                MessagePartInfo part = inMsg.addMessagePart(partName);

                /*if (isHolder(paramClasses[j], genParTypes[j]) && !isInOutParam(method, j)) {
                    LOG.log(Level.WARNING, "INVALID_WEBPARAM_MODE", getServiceClass().getName() + "."
                            + method.getName());
                }*/
                initializeParameter(part, method.getInputParams().get(j));//, genParTypes[j]);
                //part.setProperty(METHOD_PARAM_ANNOTATIONS, parAnnotations);
                //part.setProperty(PARAM_ANNOTATION, parAnnotations[j]);
              /*  if (!getJaxbAnnoMap(part).isEmpty()) {
                    op.setProperty(WRAPPERGEN_NEEDED, true);
                }*/
                if ( !method.isRpc() && !isWrapped(method)) {
                    part.setProperty(ELEMENT_NAME, q);
                }

                /*if (isHeader(method, j)) {
                    part.setProperty(HEADER, Boolean.TRUE);
                    if (method.isRpc() ) { //|| !isWrapped(method)
                        part.setElementQName(q);
                    } else {
                        part.setProperty(ELEMENT_NAME, q);
                    }
                }*/
                part.setIndex(j);

        }
        //sendEvent(FactoryBeanListener.Event.OPERATIONINFO_IN_MESSAGE_SET, op, method, inMsg);

        boolean hasOut = true;//hasOutMessage(method); 目前所有的方法都不是单路的
        if (hasOut) {
            // Setup the output message
            MessageInfo outMsg = op.createMessage(createOutputMessageName(op, method),
                    MessageInfo.Type.OUTPUT);
            op.setOutput(outMsg.getName().getLocalPart(), outMsg);
            final Class<?> returnType = method.getReturnType();
            if (!returnType.isAssignableFrom(void.class)) {
                final QName q = getOutPartName(op, method, -1);
                final QName q2 = getOutParameterName(op, method, -1);
                MessagePartInfo part = outMsg.addMessagePart(q);
                initializeParameter(part, method.getOutParam());
                if (!method.isRpc()) {
                    part.setProperty(ELEMENT_NAME, q2);
                }
               /* final Annotation[] annotations = method.getAnnotations();
                part.setProperty(METHOD_ANNOTATIONS, annotations);
                part.setProperty(PARAM_ANNOTATION, annotations);*/


                part.setIndex(0);
            }

           /* for (int j = 0; j < paramClasses.length; j++) { 这里不再处理outParam class信息

                if (isOutParam(method, j)) {
                    if (outMsg == null) {
                        outMsg = op.createMessage(createOutputMessageName(op, method),
                                MessageInfo.Type.OUTPUT);
                    }
                    QName q = getOutPartName(op, method, j);
                    QName q2 = getOutParameterName(op, method, j);

                    if (isInParam(method, j)) {
                        MessagePartInfo mpi = op.getInput().getMessagePartByIndex(j);
                        q = mpi.getName();
                        q2 = (QName)mpi.getProperty(ELEMENT_NAME);
                        if (q2 == null) {
                            q2 = mpi.getElementQName();
                        }
                    }

                    MessagePartInfo part = outMsg.addMessagePart(q);
                    part.setProperty(METHOD_PARAM_ANNOTATIONS, parAnnotations);
                    part.setProperty(PARAM_ANNOTATION, parAnnotations[j]);
                    initializeParameter(part, paramClasses[j], genParTypes[j]);
                    part.setIndex(j + 1);

                    if (!method.isRpc()) {
                        part.setProperty(ELEMENT_NAME, q2);
                    }

                    if (isInParam(method, j)) {
                        part.setProperty(MODE_INOUT, Boolean.TRUE);
                    }
                 *//*   if (isHeader(method, j)) {
                        part.setProperty(HEADER, Boolean.TRUE);
                        if (method.isRpc() ) {//|| !isWrapped(method)
                            part.setElementQName(q2);
                        } else {
                            part.setProperty(ELEMENT_NAME, q2);
                        }
                    }*//*
                }
            }*/
            //sendEvent(FactoryBeanListener.Event.OPERATIONINFO_OUT_MESSAGE_SET, op, method, outMsg);
        }

        //setting the parameterOrder that
        //allows preservation of method signatures
        //when doing java->wsdl->java
        //
        //setParameterOrder(method, paramClasses, op);

      /*  if (hasOut) {
            // Faults are only valid if not a one-way operation
            initializeFaults(intf, op, method);
        }*/
    }
 /*   protected void initializeFaults(final InterfaceInfo service,
                                    final OperationInfo op, final Method method) {
        // Set up the fault messages
        final Class<?>[] exceptionClasses = method.getExceptionTypes();
        for (int i = 0; i < exceptionClasses.length; i++) {
            Class<?> exClazz = exceptionClasses[i];

            // Ignore XFireFaults because they don't need to be declared
            if (Fault.class.isAssignableFrom(exClazz)
                    || exClazz.equals(RuntimeException.class) || exClazz.equals(Throwable.class)) {
                continue;
            }

            addFault(service, op, exClazz);
        }
    }*/
    private QName getOutParameterName(OperationInfo op, ServiceMethodInfo method, int i) {
        return new QName(modelInfo.getTargetNamespace(),method.getOutParam().getName());
    }

    private QName getOutPartName(OperationInfo op, ServiceMethodInfo method, int i) {
        String name = "return";
        if(i != -1){
            name = name + i;
        }
        return new QName(modelInfo.getTargetNamespace(),name);
    }

    private QName createOutputMessageName(OperationInfo op, ServiceMethodInfo method) {
        return new QName(modelInfo.getTargetNamespace(),method.getName()+"Response");
    }

    protected void initializeParameter(MessagePartInfo part, Param param) {
        Class rawClass = param.getParamType().getType();
        if(param.isObject()){
            String className = param.fullTypeName(StringHelper.getPkgNameByNamespace(modelInfo.getTargetNamespace()));
            try {
                rawClass = memoryClassLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new StdException("load_class_fail",e).param("className",className);
            }
        }
        Type type = null;
        /*if (type instanceof TypeVariable) { // 泛型变量，暂时不支持
            if (parameterizedTypes == null) {
                processParameterizedTypes();
            }
            TypeVariable<?> var = (TypeVariable<?>)type;
            final Object gd = var.getGenericDeclaration();
            Map<String, Class<?>> mp = parameterizedTypes.get(gd);
            if (mp != null) {
                Class<?> c = parameterizedTypes.get(gd).get(var.getName());
                if (c != null) {
                    rawClass = c;
                    type = c;
                    part.getMessageInfo().setProperty("parameterized", Boolean.TRUE);
                }
            }
        }*/
      //  part.setProperty(GENERIC_TYPE, type);
        // if rawClass is List<String>, it will be converted to array
        // and set it to type class
        if (Collection.class.isAssignableFrom(rawClass)) {
            part.setProperty(RAW_CLASS, rawClass);
        }
        part.setTypeClass(rawClass);

        if (part.getMessageInfo().getOperation().isUnwrapped()
                && Boolean.TRUE.equals(part.getProperty(HEADER))) {
            //header from the unwrapped operation, make sure the type is set for the
            //approriate header in the wrapped operation
            OperationInfo o = ((UnwrappedOperationInfo)part.getMessageInfo().getOperation())
                    .getWrappedOperation();

            if (Boolean.TRUE.equals(part.getProperty(ReflectionServiceFactoryBean.MODE_OUT))
                    || Boolean.TRUE.equals(part.getProperty(ReflectionServiceFactoryBean.MODE_INOUT))) {
                MessagePartInfo mpi = o.getOutput().getMessagePart(part.getName());
                if (mpi != null) {
                    mpi.setTypeClass(rawClass);
                    mpi.setProperty(GENERIC_TYPE, type);
                    if (Collection.class.isAssignableFrom(rawClass)) {
                        mpi.setProperty(RAW_CLASS, type);
                    }
                }
            }
            if (!Boolean.TRUE.equals(part.getProperty(ReflectionServiceFactoryBean.MODE_OUT))) {
                MessagePartInfo mpi = o.getInput().getMessagePart(part.getName());
                if (mpi != null) {
                    mpi.setTypeClass(rawClass);
                    mpi.setProperty(GENERIC_TYPE, type);
                    if (Collection.class.isAssignableFrom(rawClass)) {
                        mpi.setProperty(RAW_CLASS, type);
                    }
                }
            }
        }
    }
    private QName getInPartName(OperationInfo op, ServiceMethodInfo method, int j) {
        Param param = method.getInputParams().get(j);
        String name = null;
        if(!StringUtils.isEmpty(param.getName())){
            name = param.getName();
        }else{
            name = "arg"+j;
        }
        return new QName(modelInfo.getTargetNamespace(),name);
    }

    private QName getInParameterName(OperationInfo op, ServiceMethodInfo method, int j) {
        Param param = method.getInputParams().get(j);
        String name = null;
        if(!StringUtils.isEmpty(param.getName())){
            name = param.getName();
        }else{
            name = "arg"+j;
        }
        return new QName(modelInfo.getTargetNamespace(),name);
    }

    private Object getAction(OperationInfo op, ServiceMethodInfo m) {
        return null;
    }

    private QName getOperationName(InterfaceInfo intf, ServiceMethodInfo m) {
        return new QName(modelInfo.getTargetNamespace(),m.getName());
    }
    public static void main(String[] args) throws WSDLException {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName("User");
        classInfo.setPackageName("com.jd");
        classInfo.setFields(new ArrayList<>());
        classInfo.getFields().add(new FieldInfo("id","java.util.String"));
        classInfo.getFields().add(new FieldInfo("name","java.util.String"));

        WsdlGenerator generator = new WsdlGenerator(null);
        ServiceInfo serviceInfo = generator.buildServiceInfo();
        ServiceWSDLBuilder builder = new ServiceWSDLBuilder(null,serviceInfo);
        Definition definition = builder.build(Collections.emptyMap());
    }
}
