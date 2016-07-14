package qunar.tc.dubbocopy.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author song.xue created on 15/4/21
 * @version 1.0.0
 */
public class Executors {

    public static final int DEFAULT_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    public static final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("dc-boss-%s").build());

    public static final NioEventLoopGroup workerGroup = new NioEventLoopGroup(Executors.DEFAULT_THREAD_NUM, new ThreadFactoryBuilder().setNameFormat("dc-worker-%s").build());

    public static void shutdownAll() {
        if (!bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully().awaitUninterruptibly();
        }
        if (!workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully().awaitUninterruptibly();
        }
    }
}
