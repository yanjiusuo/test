package com.jd.workflow.console.base;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 项目名称：parent
 * 类 名 称：IpUtil
 * 类 描 述：TODO
 * 创建时间：2022-12-16 14:27
 * 创 建 人：wangxiaofei8
 */
public class IpUtil {

    /**
     * 获取本机有效的外部IP地址，而非内部的回环IP
     */
    public static String getLocalIp() {
        InetAddress address = getLocalAddress();

        if (address == null) {
            return null;
        }

        String ip = address.getHostAddress();
        if (ip == null
                || "".equals(ip.trim())
                || "0.0.0.0".equals(ip)
                || "127.0.0.1".equals(ip)) {
            ip = address.getHostName();
        }

        return ip;
    }

    private static InetAddress getLocalAddress() {
        InetAddress localAddress = null;
        try {
            //如果能直接取到正确IP就返回，通常windows下可以
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
//            e.printStackTrace();
        }

        try {
            //通过轮询网卡接口来获取IP
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
//                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Throwable e) {
//                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
//            e.printStackTrace();
        }

        return localAddress;
    }

    /**
     * 判断是否为有效合法的外部IP，而非内部回环IP
     *
     * @param address
     */
    private static boolean isValidAddress(InetAddress address) {
        if ((address == null) || (address.isLoopbackAddress())) {
            return false;
        }

        String ip = address.getHostAddress();

        return (ip != null) && (!"0.0.0.0".equals(ip)) && (!"127.0.0.1".equals(ip)) && (IP_PATTERN.matcher(ip).matches());
    }

    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public static void main(String[] args) {
        System.out.println(IpUtil.getLocalIp());
    }

}
