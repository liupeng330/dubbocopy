package qunar.tc.dubbocopy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import qunar.metrics.Metrics;
import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.balance.LoadBalance;
import qunar.tc.dubbocopy.conn.Connection;
import qunar.tc.dubbocopy.conn.ConnectionPool;
import qunar.tc.dubbocopy.request.DubboRequestInfo;
import qunar.tc.dubbocopy.request.RawByteBufRequest;
import qunar.tc.dubbocopy.request.Request;
import qunar.tc.dubbocopy.router.RouterService;
import qunar.tc.dubbocopy.util.Monitor;

import java.util.List;

/**
 * @author song.xue created on 15/4/23
 * @version 1.0.0
 */

/**
 * @author kelly.li
 */
@ChannelHandler.Sharable
public class DispatchHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchHandler.class);

    private static final ByteBuf BUF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(1).writeByte(1));

    @Autowired
    private RouterService routerService;

    @Autowired
    private LoadBalance loadBalance;

    private ConnectionPool connectionPool;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Request))
            return;
        final Request request = (Request) msg;
        LOGGER.info("接收 {}.{} 的数据", request.getServiceName(), request.getMethodName());
        final DubboRequestInfo requestInfo = new DubboRequestInfo(request.getServiceName(), request.getMethodName());
        List<Group> groups = routerService.selectGroups(requestInfo);
        if (groups == null || groups.isEmpty()) {
            LOGGER.info("没有router，不发送请求 {}", requestInfo);
            Metrics.meter(Monitor.REQUEST_NO_ROUTER).tag("service", requestInfo.getServiceName()).get().mark();
            request.release();
            return;
        }
        LOGGER.info("接收 {}.{} 的数据,数据匹配{}个组", request.getServiceName(), request.getMethodName(), groups.size());
        dispatch(groups, request);
    }

    private void dispatch(List<Group> groups, Request request) {
        for (Group group : groups) {
            for (int i = 0; i < group.getN(); i++) {
                Target target = loadBalance.select(group);
                RawByteBufRequest rawRequest = new RawByteBufRequest.Builder(request.getServiceName(), request.getMethodName(), group.getName(), group.getN(), target,
                        request.getRawData().duplicate().retain()).build();
                send(rawRequest);
            }
        }

    }

    private void send(final RawByteBufRequest rawRequest) {
        Monitor.requestSent.mark();
        Connection conn = connectionPool.getConnection(rawRequest.getTarget());
        conn.send(rawRequest);
        LOGGER.info("向{}发送数据", rawRequest.getTarget());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive())
            ctx.writeAndFlush(BUF.duplicate());
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

}
