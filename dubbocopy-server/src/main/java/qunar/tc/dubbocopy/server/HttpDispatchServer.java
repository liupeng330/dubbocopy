package qunar.tc.dubbocopy.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import qunar.tc.dubbocopy.handler.FullHttpRequestDecoder;

/**
 * @author kelly.li
 * @date 2015-07-16
 */
public class HttpDispatchServer extends AbstractDispatchServer {

    public HttpDispatchServer(int serverPort) {
        super(serverPort);
    }

    @Override
    public void initHandler(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("httpDecoder", new HttpRequestDecoder());
        ch.pipeline().addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast("fullHttpEncoder", new FullHttpRequestDecoder());
    }

}
