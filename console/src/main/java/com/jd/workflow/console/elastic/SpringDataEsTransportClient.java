package com.jd.workflow.console.elastic;


import com.google.common.base.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Create by gouyaoqing on 2018/10/25
 */
public class SpringDataEsTransportClient extends PreBuiltTransportClient {
    private static Logger logger = LoggerFactory.getLogger(SpringDataEsTransportClient.class);

    public static final String PREFIX = "request.headers";
    public static final String BASIC_AUTH_HEADER = "Authorization";

    public static SpringDataEsTransportClient getInstance(Map<String, String> settingsMap, List<String> nodes, Class<? extends Plugin>... plugins) {
        Settings.Builder settingsBuilder = Settings.builder();
        if (settingsMap != null) {
            settingsMap.forEach((key, value) -> {
                settingsBuilder.put(key, value);
            });
        }

        //处理用户名密码
        String username = settingsBuilder.get("username");
        String password = settingsBuilder.get("password");
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            settingsBuilder.put(PREFIX + "." + BASIC_AUTH_HEADER, basicAuthHeaderValue(username, password));
            settingsBuilder.remove("username");
            settingsBuilder.remove("password");
        }

        SpringDataEsTransportClient client = new SpringDataEsTransportClient(settingsBuilder.build(), plugins);

        //处理节点ip、port
        if (nodes != null && nodes.size() > 0) {
            nodes.forEach(node -> {
                String ipPort[] = node.split(":");
                try {
                    client.addTransportAddress(new TransportAddress(InetAddress.getByName(ipPort[0]), Integer.parseInt(ipPort[1])));
                } catch (UnknownHostException e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
        return client;
    }

    public SpringDataEsTransportClient(Settings settings, Class<? extends Plugin>... plugins) {
        super(settings, plugins);
    }

    public SpringDataEsTransportClient(Settings settings, Collection<Class<? extends Plugin>> plugins) {
        super(settings, plugins);
    }

    public SpringDataEsTransportClient(Settings settings, Collection<Class<? extends Plugin>> plugins, HostFailureListener hostFailureListener) {
        super(settings, plugins, hostFailureListener);
    }

    private static String basicAuthHeaderValue(String username, String passwd) {
        CharBuffer chars = CharBuffer.allocate(username.length() + passwd.length() + 1);
        byte[] charBytes = null;
        try {
            chars.put(username).put(':').put(passwd);
            charBytes = toUtf8Bytes(chars.array());

            //TODO we still have passwords in Strings in headers. Maybe we can look into using a CharSequence?
            String basicToken = Base64.getEncoder().encodeToString(charBytes);
            return "Basic " + basicToken;
        } finally {
            Arrays.fill(chars.array(), (char) 0);
            if (charBytes != null) {
                Arrays.fill(charBytes, (byte) 0);
            }
        }
    }

    private static byte[] toUtf8Bytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}
