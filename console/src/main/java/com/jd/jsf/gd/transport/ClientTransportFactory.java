//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.jd.jsf.gd.transport;

import com.jd.jsf.gd.error.InitErrorException;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.jsf.gd.util.NetUtils;
import com.jd.jsf.gd.util.Constants.ProtocolType;
import com.jd.jsf.gd.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTransportFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientTransportFactory.class);
    private static final Map<String, ClientTransport> connectionPool = new ConcurrentHashMap();
    private static final Map<ClientTransport, AtomicInteger> refCountPool = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap();

    public ClientTransportFactory() {
    }

    public static ClientTransport getClientTransport(ClientTransportConfig config) {
        ProtocolType type = config.getProvider().getProtocolType();
        if (type != ProtocolType.rest && type != ProtocolType.jaxws && type != ProtocolType.webservice) {
            String key = NetUtils.getClientTransportKey(config.getProvider().getProtocolType().name(), config.getProvider().getIp(), config.getProvider().getPort());
            ClientTransport conn = (ClientTransport)connectionPool.get(key);
            AtomicInteger count = null;
            if (conn == null) {
                Object lock = lockMap.get(key);
                if (lock == null) {
                    lock = new Object();
                    Object temp = lockMap.putIfAbsent(key, lock);
                    if (temp != null) {
                        lock = temp;
                    }
                }

                synchronized(lock) {
                    conn = (ClientTransport)connectionPool.get(key);
                    if (conn == null) {
                        logger.debug("Try connect to provider:{}", config.getProvider());
                        conn = initTransport(config);
                        connectionPool.put(key, conn);
                        count = (AtomicInteger)refCountPool.get(conn);
                        if (count == null) {
                            count = new AtomicInteger(0);
                            refCountPool.put(conn, count);
                        }
                    }
                }

                lockMap.remove(key);
            }

            count = (AtomicInteger)refCountPool.get(conn);
            int currentCount = count.incrementAndGet();
            logger.debug("Client transport {} of {} , current ref count is: {}", new Object[]{conn, NetUtils.channelToString(conn.getLocalAddress(), conn.getRemoteAddress()), currentCount});
            return conn;
        } else {
            return initTransport(config);
        }
    }

    public static Map<String, ClientTransport> getConnectionPool() {
        return connectionPool;
    }

    public static void releaseTransport(ClientTransport clientTransport, int timeout) {
        if (clientTransport != null) {
            AtomicInteger integer = (AtomicInteger)refCountPool.get(clientTransport);
            if (integer != null) {
                int currentCount = ((AtomicInteger)refCountPool.get(clientTransport)).decrementAndGet();
                InetSocketAddress local = clientTransport.getLocalAddress();
                InetSocketAddress remote = clientTransport.getRemoteAddress();
                logger.debug("Client transport {} of {} , current ref count is: {}", new Object[]{clientTransport, NetUtils.channelToString(local, remote), currentCount});
                if (currentCount <= 0) {
                    String ip = clientTransport.getConfig().getProvider().getIp();
                    int port = remote.getPort();
                    String key = NetUtils.getClientTransportKey(clientTransport.getConfig().getProvider().getProtocolType().name(), ip, port);
                    logger.info("Shutting down client transport {} now..", NetUtils.channelToString(local, remote));
                    connectionPool.remove(key);
                    refCountPool.remove(clientTransport);
                    if (timeout > 0) {
                        int count = clientTransport.currentRequests();
                        if (count > 0) {
                            long start = JSFContext.systemClock.now();
                            logger.info("There are {} outstanding call in transport, will shutdown util return", count);

                            while(clientTransport.currentRequests() > 0 && JSFContext.systemClock.now() - start < (long)timeout) {
                                try {
                                    Thread.sleep(10L);
                                } catch (InterruptedException var13) {
                                }
                            }
                        }
                    }

                    clientTransport.shutdown();
                }

            }
        }
    }

    private static ClientTransport initTransport(ClientTransportConfig config) {
        ClientTransport clientTransport = null;
        ProtocolType protocolType = config.getProvider().getProtocolType();
        switch(protocolType) {
            case jsf:
                clientTransport = instanceTransport(config);
                break;
            case dubbo:
                clientTransport = instanceTransport(config);
                break;
            case rest:
                clientTransport = new HttpRestClientTransport(config);
                break;
            case jaxws:
            case webservice:
                clientTransport = new HttpWsClientTransport(config);
                break;
            case grpc:
                clientTransport = instanceTransport(config);
                break;
            default:
                logger.error("Unsupported consumer protocol: {}", protocolType);
                throw new InitErrorException("unsupported consumer protocol:" + protocolType);
        }

        return (ClientTransport)clientTransport;
    }

    private static ClientTransport instanceTransport(ClientTransportConfig config) {
        ClientTransport clientTransport = null;
        Channel channel = null;
        boolean succ = false;

        try {
            channel = BuildChannel(config);
            ProtocolType protocolType = config.getProvider().getProtocolType();
            switch(protocolType) {
                case jsf:
                case dubbo:
                case grpc:
                    clientTransport = (new JSFClientTransport(channel)).setClientTransportConfig(config);
                    bindTransport(clientTransport, channel);
                    succ = true;
                    return clientTransport;
                default:
                    logger.error("Unsupported consumer protocol: {}", protocolType);
                    throw new InitErrorException("unsupported consumer protocol:" + protocolType);
            }
        } catch (InitErrorException var8) {
            logger.debug(var8.getMessage(), var8);
            throw var8;
        } finally {
            if (!succ && channel != null) {
                channel.close();
            }

        }
    }

    private static ClientTransport bindTransport(ClientTransport clientTransport, Channel channel) {
        ClientChannelHandler clientChannelHandler = (ClientChannelHandler)channel.pipeline().get("JSF_CLIENT_CHANNELHANDLE");
        clientChannelHandler.setClientTransport((AbstractTCPClientTransport)clientTransport);
        return clientTransport;
    }

    public static ClientTransport reconn(AbstractTCPClientTransport clientTransport) {
        Channel channel = BuildChannel(clientTransport.getConfig());
        bindTransport(clientTransport, channel);
        clientTransport.setChannel(channel);
        return clientTransport;
    }

    public static Channel BuildChannel(ClientTransportConfig transportConfig) {
        EventLoopGroup eventLoopGroup = transportConfig.getEventLoopGroup();
        Channel channel = null;
        String host = transportConfig.getProvider().getIp();
        int port = transportConfig.getProvider().getPort();
        int connectTimeout = transportConfig.getConnectionTimeout();

        try {
            Bootstrap bootstrap = new Bootstrap();
            ((Bootstrap)bootstrap.group(eventLoopGroup)).channel(NettyHelper.getSocketChannel(transportConfig.isUseEpoll()));
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledBufHolder.getInstance());
            bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, transportConfig.getLowWaterMark());
            bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, transportConfig.getHighWaterMark());
            bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
            ClientChannelInitializer initializer = new ClientChannelInitializer(transportConfig);
            bootstrap.handler(initializer);
            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.awaitUninterruptibly((long)connectTimeout, TimeUnit.MILLISECONDS);
            if (channelFuture.isSuccess()) {
                channel = channelFuture.channel();
                if (NetUtils.toAddressString((InetSocketAddress)channel.remoteAddress()).equals(NetUtils.toAddressString((InetSocketAddress)channel.localAddress()))) {
                    channel.close();
                    throw new InitErrorException("Failed to connect " + host + ":" + port + ". Cause by: Remote and local address are the same");
                } else {
                    return channel;
                }
            } else {
                Throwable cause = channelFuture.cause();
                throw new InitErrorException("Failed to connect " + host + ":" + port + (cause != null ? ". Cause by: " + cause.getMessage() : "."));
            }
        } catch (InitErrorException var10) {
            throw var10;
        } catch (Exception var11) {
            String errorStr = "Failed to build channel for host:" + host + " port:" + port + ". Cause by: " + var11.getMessage();
            InitErrorException initException = new InitErrorException(errorStr, var11);
            throw initException;
        }
    }

    public static JSFClientTransport getTransportByKey(String key) {
        return (JSFClientTransport)connectionPool.get(key);
    }

    public static void closeAll() {
        logger.info("Shutdown all JSF client transport now...");

        try {
            Iterator var0 = connectionPool.entrySet().iterator();

            while(var0.hasNext()) {
                Entry<String, ClientTransport> entrySet = (Entry)var0.next();
                ClientTransport clientTransport = (ClientTransport)entrySet.getValue();
                if (clientTransport.isOpen()) {
                    clientTransport.shutdown();
                }
            }
        } catch (Exception var6) {
            logger.error(var6.getMessage(), var6);
        } finally {
            ClientTransportConfig.closeEventGroup();
        }

    }

    public static void checkFuture() {
        Iterator var0 = connectionPool.entrySet().iterator();

        while(var0.hasNext()) {
            Entry entrySet = (Entry)var0.next();

            try {
                ClientTransport clientTransport = (ClientTransport)entrySet.getValue();
                if (clientTransport instanceof AbstractTCPClientTransport) {
                    AbstractTCPClientTransport aClientTransport = (AbstractTCPClientTransport)clientTransport;
                    aClientTransport.checkFutureMap();
                }
            } catch (Exception var4) {
                logger.error(var4.getMessage(), var4);
            }
        }

    }
}
