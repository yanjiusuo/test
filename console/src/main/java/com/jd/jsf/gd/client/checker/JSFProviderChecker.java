package com.jd.jsf.gd.client.checker;




import com.jd.jsf.gd.client.TelnetClient;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.registry.Provider;
import com.jd.jsf.gd.util.CommonUtils;
import com.jd.jsf.gd.util.JSFLogicSwitch;
import com.jd.jsf.gd.util.JsonUtils;
import com.jd.jsf.gd.util.StringUtils;
import com.jd.jsf.gd.util.Constants.ProtocolType;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSFProviderChecker implements ProviderChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSFProviderChecker.class);
    private Integer jsfVersion;

    public JSFProviderChecker() {
    }

    public ProviderCheckedResult checkProvider(Provider provider, ConsumerConfig consumerConfig) {
        ProviderCheckedResult checkedResult = new ProviderCheckedResult();
        if (provider.getProtocolType() == ProtocolType.jsf && !JSFLogicSwitch.IS_OPEN_MESH) {
            boolean ifaceIdDoChecked = false;

            for(int i = 0; i < 2; ++i) {
                TelnetClient client = new TelnetClient(provider.getIp(), provider.getPort(), 1000, 1000);

                try {
                    int realVersion = this.checkProviderVersion(client, provider);
                    if (realVersion != provider.getJsfVersion()) {
                        provider.setJsfVersion(realVersion);
                    }

                    if (!ifaceIdDoChecked) {
                        String ifaceId = consumerConfig.getIfaceId();
                        if (StringUtils.isNotEmpty(ifaceId)) {
                            provider.setInvocationOptimizing(this.checkInvocationOptimizing(client, consumerConfig.getInterfaceId(), ifaceId));
                        }

                        ifaceIdDoChecked = true;
                    }

                    checkedResult.setProviderExportedFully(this.checkProviderExportedFully(client, provider, consumerConfig));
                    ProviderCheckedResult var14 = checkedResult;
                    return var14;
                } catch (Exception var12) {
                    LOGGER.warn(var12.getMessage());
                } finally {
                    client.close();
                }
            }

            return checkedResult;
        } else {
            checkedResult.setProviderExportedFully(true);
            return checkedResult;
        }
    }

    public void resetChecker() {
        this.jsfVersion = null;
    }

    private int checkProviderVersion(TelnetClient client, Provider provider) throws IOException {
        if (this.jsfVersion == null) {
            String versionStr = client.telnetJSF("version");

            try {
                Map map = (Map)JsonUtils.parseObject(versionStr, Map.class);
                this.jsfVersion = CommonUtils.parseInt(StringUtils.toString(map.get("jsfVersion")), provider.getJsfVersion());
                return this.jsfVersion;
            } catch (Exception var5) {
            }
        }

        return this.jsfVersion == null ? 0 : this.jsfVersion;
    }

    private boolean checkInvocationOptimizing(TelnetClient client, String ifaceName, String ifaceId) throws IOException {
        if (this.jsfVersion >= 1500) {
            String result = client.telnetJSF("check iface " + ifaceName + " " + ifaceId);
            return "1".equals(result);
        } else {
            return false;
        }
    }

    private boolean checkProviderExportedFully(TelnetClient client, Provider provider, ConsumerConfig consumerConfig) throws IOException {
        return true;
    }
}