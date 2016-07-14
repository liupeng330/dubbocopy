package qunar.tc.dubbocopy.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import qunar.tc.dubbocopy.conn.Connection;
import qunar.tc.dubbocopy.conn.ConnectionPool;
import qunar.tc.dubbocopy.conn.NettyConnection;

@ChannelHandler.Sharable
public class DiscardHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionPool connectionPool;

    public DiscardHandler(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Object conn = ctx.channel().attr(NettyConnection.connKey).get();
        if (conn == null) return;
        connectionPool.removeConnection((Connection) conn);
    }

    // 安全释放消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ReferenceCountUtil.safeRelease(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        Object conn = ctx.channel().attr(NettyConnection.connKey).get();
        if (conn == null) return;
        connectionPool.returnConnection((Connection) conn);
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}