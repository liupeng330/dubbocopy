package qunar.tc.dubbocopy.conn;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.handler.DiscardHandler;
import qunar.tc.dubbocopy.handler.IdleCloseHandler;
import qunar.tc.dubbocopy.request.RawByteBufRequest;
import qunar.tc.dubbocopy.util.Executors;
import qunar.tc.dubbocopy.util.Monitor;

import java.net.SocketAddress;

/**
 * @author kelly.li
 * @date 2015-06-30
 */
public class NettyConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    @Value("${dubbocopy.client.connectTimeout}")
    private int connectionTimeoutMs;

    @Value("${dubbocopy.client.idleTimeoutSeconds}")
    private int idleTimeoutSeconds;

    private DiscardHandler discardHandler;

    private IdleCloseHandler idleCloseHandler;

    private String strVal = "";

    private final Target target;
    private final String host;
    private final int port;

    private volatile boolean closed = false;

    private volatile Channel channel;

    private Bootstrap bootstrap;

    public static final AttributeKey<Connection> connKey = AttributeKey.valueOf("conn");

    public NettyConnection(Target target, int connectionTimeoutMs, int idleTimeoutSeconds, DiscardHandler discardHandler, IdleCloseHandler idleCloseHandler) {
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.idleTimeoutSeconds = idleTimeoutSeconds;
        this.discardHandler = discardHandler;
        this.idleCloseHandler = idleCloseHandler;
        this.host = target.getHost();
        this.port = target.getPort();
        this.target = target;

        open();
        try {
            ChannelFuture future = connect();
            if (future == null)
                return;
            this.channel = future.await().channel();
            this.channel.attr(connKey).set(this);
        } catch (Exception e) {
            LOGGER.error("build consumer connection error", e);
        }
    }

    private void open() {
        bootstrap = new Bootstrap()
                .group(Executors.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, false)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, idleTimeoutSeconds));
                        ch.pipeline().addLast(idleCloseHandler);
                        ch.pipeline().addLast(discardHandler);
                    }
                });
    }

    private ChannelFuture connect() {
        LOGGER.info("connect to {}:{}", host, port);
        return bootstrap.connect(host, port);
    }

    public void close() {
        LOGGER.warn("close connection: {} for {}:{}", this.channel, host, port);
        closed = true;
        if (this.channel == null)
            return;
        this.channel.close();
    }

    public String key() {
        if (this.channel == null) return strVal;
        if (strVal.length() > 0) return strVal;

        SocketAddress remoteAddr = this.channel.remoteAddress();
        SocketAddress localAddr = this.channel.localAddress();
        if (remoteAddr != null) {
            SocketAddress srcAddr;
            SocketAddress dstAddr;

            srcAddr = localAddr;
            dstAddr = remoteAddr;

            strVal = String.format("[%s => %s]", srcAddr, dstAddr);
        } else if (localAddr != null) {
            strVal = String.format("[%s]", localAddr);
        }
        return strVal;
    }

    public Target target() {
        return this.target;
    }

    public void send(final RawByteBufRequest request) {
        if (channel == null || !channel.isActive())
            return;
        if (!channel.isWritable()) {
            request.onUnWritable();
            Monitor.sendBufferOverflow.inc();
            return;
        }
        this.channel.writeAndFlush(request.getByteBuf()).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    request.onWriteSuccess();
                } else {
                    request.onWriteFailed(future.cause());
                }
            }
        });
    }

    boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "client[" + host + ":" + port + "]";
    }

}
