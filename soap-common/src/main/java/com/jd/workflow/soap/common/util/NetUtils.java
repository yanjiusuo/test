package com.jd.workflow.soap.common.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class NetUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    public static final String ANYHOST = "0.0.0.0";
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    public static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    public NetUtils() {
    }

    public static boolean isInvalidPort(int port) {
        return port > 65535 || port < 0;
    }

    public static boolean isRandomPort(int port) {
        return port < 0;
    }



    public static boolean isLocalHost(String host) {
        return StringUtils.isNotBlank(host) && (LOCAL_IP_PATTERN.matcher(host).matches() || "localhost".equalsIgnoreCase(host));
    }

    public static boolean isAnyHost(String host) {
        return "0.0.0.0".equals(host);
    }

    public static boolean isIPv4Host(String host) {
        return StringUtils.isNotBlank(host) && IPV4_PATTERN.matcher(host).matches();
    }

    private static boolean isInvalidLocalHost(String host) {
        return StringUtils.isBlank(host) || isAnyHost(host) || isLocalHost(host);
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address != null && !address.isLoopbackAddress()) {
            String name = address.getHostAddress();
            return name != null && !isAnyHost(name) && !isLocalHost(name) && isIPv4Host(name);
        } else {
            return false;
        }
    }

    public static boolean isHostInNetworkCard(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (Exception var2) {
            return false;
        }
    }

    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null ? null : address.getHostAddress();
    }

    public static InetAddress getLocalAddress() {
        InetAddress localAddress = null;

        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable var6) {
            logger.warn("Error when retriving ip address: " + var6.getMessage(), var6);
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = (NetworkInterface) interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = (InetAddress) addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable var5) {
                                    logger.warn("Error when retriving ip address: " + var5.getMessage(), var5);
                                }
                            }
                        }
                    } catch (Throwable var7) {
                        logger.warn("Error when retriving ip address: " + var7.getMessage(), var7);
                    }
                }
            }
        } catch (Throwable var8) {
            logger.warn("Error when retriving ip address: " + var8.getMessage(), var8);
        }

        logger.error("Can't get valid host, will use 127.0.0.1 instead.");
        return localAddress;
    }

    public static String toAddressString(InetSocketAddress address) {
        return address == null ? "" : toIpString(address) + ":" + address.getPort();
    }

    public static String toIpString(InetSocketAddress address) {
        if (address == null) {
            return null;
        } else {
            InetAddress inetAddress = address.getAddress();
            return inetAddress == null ? address.getHostName() : inetAddress.getHostAddress();
        }
    }

    public static String getLocalHostByRegistry(String registryIp) {
        String host = null;
        if (registryIp != null && registryIp.length() > 0) {
            List<InetSocketAddress> addrs = getIpListByRegistry(registryIp);

            for (int i = 0; i < addrs.size(); ++i) {
                InetAddress address = getLocalHostBySocket((InetSocketAddress) addrs.get(i));
                if (address != null) {
                    host = address.getHostAddress();
                    if (host != null && !isInvalidLocalHost(host)) {
                        return host;
                    }
                }
            }
        }

        if (isInvalidLocalHost(host)) {
            host = getLocalHost();
        }

        return host;
    }

    private static InetAddress getLocalHostBySocket(InetSocketAddress remoteAddress) {
        InetAddress host = null;

        try {
            Socket socket = new Socket();

            try {
                socket.connect(remoteAddress, 1000);
                host = socket.getLocalAddress();
            } finally {
                try {
                    socket.close();
                } catch (Throwable var10) {
                }

            }
        } catch (Exception var12) {
            logger.warn("Can not connect to host {}, cause by :{}", remoteAddress.toString(), var12.getMessage());
        }

        return host;
    }

    public static List<InetSocketAddress> getIpListByRegistry(String registryIp) {
        List<String[]> ips = new ArrayList();
        String defaultPort = null;
        String[] srcIps = registryIp.split(",");
        String[] var4 = srcIps;
        int j = srcIps.length;

        for (int var6 = 0; var6 < j; ++var6) {
            String add = var4[var6];
            int a = add.indexOf("://");
            if (a > -1) {
                add = add.substring(a + 3);
            }

            String[] s1 = add.split(":");
            if (s1.length > 1) {
                if (defaultPort == null && s1[1] != null && s1[1].length() > 0) {
                    defaultPort = s1[1];
                }

                ips.add(new String[]{s1[0], s1[1]});
            } else {
                ips.add(new String[]{s1[0], defaultPort});
            }
        }

        List<InetSocketAddress> ads = new ArrayList();

        for (j = 0; j < ips.size(); ++j) {
            String[] ip = (String[]) ips.get(j);

            try {
                InetSocketAddress address = new InetSocketAddress(ip[0], Integer.parseInt(ip[1] == null ? defaultPort : ip[1]));
                ads.add(address);
            } catch (Exception var10) {
            }
        }

        return ads;
    }

    public static boolean isMatchIPByPattern(String whitelist, String localIP) {
        if (StringUtils.isNotBlank(whitelist)) {
            if ("*".equals(whitelist)) {
                return true;
            }

            String[] var2 = whitelist.replace(',', ';').split(";", -1);
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String ips = var2[var4];

                try {
                    String regex;
                    Pattern pattern;
                    if (ips.contains("*")) {
                        regex = ips.trim().replace(".", "\\.").replace("*", ".*");
                        pattern = Pattern.compile(regex);
                        if (pattern.matcher(localIP).find()) {
                            return true;
                        }
                    } else if (!isIPv4Host(ips)) {
                        regex = ips.trim().replace(".", "\\.");
                        pattern = Pattern.compile(regex);
                        if (pattern.matcher(localIP).find()) {
                            return true;
                        }
                    } else if (ips.equals(localIP)) {
                        return true;
                    }
                } catch (Exception var8) {
                    logger.warn("syntax of pattern {} is invalid", ips);
                }
            }
        }

        return false;
    }

    public static String connectToString(InetSocketAddress local, InetSocketAddress remote) {
        return toAddressString(local) + " <-> " + toAddressString(remote);
    }

    public static String channelToString(SocketAddress local1, SocketAddress remote1) {
        try {
            InetSocketAddress local = (InetSocketAddress) local1;
            InetSocketAddress remote = (InetSocketAddress) remote1;
            return toAddressString(local) + " -> " + toAddressString(remote);
        } catch (Exception var4) {
            return local1 + "->" + remote1;
        }
    }

    public static boolean canTelnet(String ip, int port, int timeout) {
        Socket socket = null;

        boolean var5;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            boolean var4 = socket.isConnected();
            return var4;
        } catch (Exception var15) {
            var5 = false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException var14) {
                }
            }

        }

        return var5;
    }

    public static String getTransportKey(String ip, int port) {
        return ip + "::" + port;
    }

    public static String getClientTransportKey(String protocolName, String ip, int port) {
        return protocolName + "::" + ip + "::" + port;
    }

}