package qunar.tc.dubbocopy.test.a;
//package qunar.tc.dubbocopy.client;
//
//import com.google.common.collect.Maps;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.buffer.PooledByteBufAllocator;
//import io.netty.channel.*;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.timeout.IdleStateHandler;
//import io.netty.util.ReferenceCountUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import qunar.metrics.Gauge;
//import qunar.metrics.Metrics;
//import qunar.tc.dubbocopy.api.model.Target;
//import qunar.tc.dubbocopy.server.handler.IdleCloseHandler;
//import qunar.tc.dubbocopy.util.Executors;
//import qunar.tc.dubbocopy.util.Monitor;
//
//import javax.annotation.PostConstruct;
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.util.Map;
//
///**
// * @author song.xue created on 15/4/23
// * @version 1.0.0
// */
//public class RawByteBufClient {
//    private static final Logger LOGGER = LoggerFactory.getLogger(RawByteBufClient.class);
//
//    @Value("${dubbocopy.client.connectTimeout}")
//    private int connectionTimeoutMs;
//
//    @Value("${dubbocopy.client.idleTimeoutSeconds}")
//    private int idleTimeoutSeconds;
//
//    private final Map<Target, Channel> establishedChannels;
//
//    private final DiscardHandler discardHandler;
//
//    private final IdleCloseHandler idleCloseHandler;
//
//    public RawByteBufClient() {
//        Metrics.gauge("tc.dubbocopy.server.establishedChannels").call(new Gauge() {
//            @Override
//            public double getValue() {
//                return establishedChannels.size();
//            }
//        });
//        this.establishedChannels = Maps.newConcurrentMap();
//        this.discardHandler = new DiscardHandler(establishedChannels);
//        this.idleCloseHandler = new IdleCloseHandler();
//    }
//
//    public void executeRequest(RawByteBufRequest request) {
//        connect(request);
//    }
//
//    private void connect(final RawByteBufRequest request) {
//        if (tryToUseEstablishedChannel(request)) {
//            Monitor.useOldConnection.mark();
//            LOGGER.debug("使用已有连接 {}", request);
//            return;
//        }
//        LOGGER.debug("使用新连接 {}", request);
//        Monitor.createNewConnection.mark();
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(Executors.workerGroup)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.TCP_NODELAY, false)
//                .option(ChannelOption.SO_REUSEADDR, true)
//                .option(ChannelOption.AUTO_READ, true)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs)
//                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
//                .handler(new ChannelInitializer<NioSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioSocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new IdleStateHandler(0, 0, idleTimeoutSeconds));
//                        ch.pipeline().addLast(idleCloseHandler);
//                        ch.pipeline().addLast(discardHandler);
//                    }
//                });
//        bootstrap.connect(request.getTarget().getHost(), request.getTarget().getPort()).addListener(new ChannelFutureListener() {
//
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (!future.isSuccess()) {
//                    LOGGER.error("request {} connect failed", request);
//                    request.getListener().onConnectFailed(future.cause());
//                    return;
//                }
//                establishedChannels.put(request.getTarget(), future.channel());
//                request.getListener().onConnected();
//                sendData(request, future.channel());
//            }
//        });
//    }
//
//    private boolean tryToUseEstablishedChannel(RawByteBufRequest request) {
//        Channel channel = establishedChannels.get(request.getTarget());
//        if (channel == null) {
//            return false;
//        }
//        if (!channel.isActive()) {
//            establishedChannels.remove(request.getTarget());
//            return false;
//        }
//        sendData(request, channel);
//        return true;
//    }
//
//    private void sendData(final RawByteBufRequest request, Channel channel) {
//        if (!channel.isWritable()) {
//            request.getListener().onUnWritable();
//            Monitor.sendBufferOverflow.inc();
//            return;
//        }
//        channel.writeAndFlush(request.getByteBuf()).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()) {
//                    request.getListener().onWriteSuccess();
//                } else {
//                    request.getListener().onWriteFailed(future.cause());
//                }
//            }
//        });
//    }
//
//    private static final int CLEAR_CHANNEL_INTERVAL_MILLI = 30000;
//
//
//    @PostConstruct
//    public void startClearThread() {
//        Thread clearThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(CLEAR_CHANNEL_INTERVAL_MILLI);
//                    } catch (InterruptedException e) {
//                        LOGGER.error("清除线程被打断{}", e);
//                        Thread.interrupted();
//                    }
//                    RawByteBufClient.this.clearClosedChannels();
//                }
//            }
//        });
//        clearThread.setDaemon(true);
//        clearThread.setName("clear-dead-channel-thread");
//        clearThread.start();
//    }
//
//    private void clearClosedChannels() {
//    	//如果channel 不活动清楚
//        LOGGER.info("当前共{}个连接", establishedChannels.size());
//        for (Map.Entry<Target, Channel> entry : establishedChannels.entrySet()) {
//            if (!entry.getValue().isActive()) {
//                establishedChannels.remove(entry.getKey());
//            }
//        }
//        LOGGER.info("清除后共{}个连接", establishedChannels.size());
//    }
//
//    @ChannelHandler.Sharable
//    private static class DiscardHandler extends ChannelInboundHandlerAdapter {
//
//        private final Map<Target, Channel> channels;
//
//        public DiscardHandler(Map<Target, Channel> channels) {
//            this.channels = channels;
//        }
//
//        @Override
//        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        	//如果channel 不活动清楚
//            SocketAddress address = ctx.channel().remoteAddress();
//            if (address == null) return;
//            if (!(address instanceof InetSocketAddress)) return;
//            InetSocketAddress socketAddress = (InetSocketAddress) address;
//            channels.remove(new Target(socketAddress.getHostName(), socketAddress.getPort()));
//        }
//
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ReferenceCountUtil.safeRelease(msg);
//        }
//
//        @Override
//        public boolean isSharable() {
//            return true;
//        }
//    }
//
//}
