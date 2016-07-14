package qunar.tc.dubbocopy.server;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.ThreadFactory;

/**
 * Created by zhaohui.yu
 * 4/27/15
 */
public class ReuseNioEventLoopGroup extends NioEventLoopGroup {
    private final EventLoop eventLoop;

    public ReuseNioEventLoopGroup(EventLoop eventLoop) {
        super(1);
        this.eventLoop = eventLoop;
    }

    @Override
    protected EventExecutor newChild(ThreadFactory threadFactory, Object... args) throws Exception {
        return this.eventLoop;
    }
}
