package qunar.tc.dubbocopy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import qunar.tc.dubbocopy.handler.DispatchHandler;
import qunar.tc.dubbocopy.handler.IdleCloseHandler;
import qunar.tc.dubbocopy.util.Executors;

/**
 * @author song.xue created on 15/4/21
 * @version 1.0.0
 */
public abstract class AbstractDispatchServer implements InitializingBean, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDispatchServer.class);

    private Channel ch;

    private final int serverPort;

    private DispatchHandler dispatchHandler;

    @Value("${dubbocopy.server.writeBufferHighWaterMark}")
    private int writeBufferHighWaterMark;

    @Value("${dubbocopy.server.writeBufferLowWaterMark}")
    private int writeBufferLowWaterMark;

    @Value("${dubbocopy.server.readIdleTimeoutSeconds}")
    private int readerIdleTimeoutSeconds;

    public AbstractDispatchServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void afterPropertiesSet() throws Exception {
        final IdleCloseHandler idleCloseHandler = new IdleCloseHandler();

        ServerBootstrap b = new ServerBootstrap();
        //端口重启可重用
        b.option(ChannelOption.SO_REUSEADDR, true);
        b.option(ChannelOption.AUTO_READ, true);
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
        b.childOption(ChannelOption.TCP_NODELAY, true);
        b.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, writeBufferHighWaterMark);
        b.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, writeBufferLowWaterMark);
        b.group(Executors.bossGroup, Executors.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("idle", new IdleStateHandler(readerIdleTimeoutSeconds, 0, 0));
                        ch.pipeline().addLast("idleClose", idleCloseHandler);
                        initHandler(ch);
                        ch.pipeline().addLast("dispatch", dispatchHandler);

                    }
                });
        //绑定到一个30880监听连接
        ch = b.bind(serverPort).await().channel();
    }

    public abstract void initHandler(SocketChannel ch) throws Exception;

    public void destroy() throws Exception {
        try {
            LOGGER.info("准备关闭Netty服务器, 端口 {}", serverPort);
            // 阻塞, 等待断开连接和关闭操作完成
            if (ch != null && ch.isActive()) {
                ch.close().awaitUninterruptibly();
            }
            Executors.shutdownAll();
            LOGGER.info("成功关闭Netty服务器, 端口 {}", serverPort);
        } catch (Exception e) {
            LOGGER.warn("关闭Netty服务器时出错, 端口 {}", serverPort, e);
        }
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }
}
