package qunar.tc.dubbocopy.server;

import io.netty.channel.socket.SocketChannel;
import qunar.tc.dubbocopy.handler.DubboDecodeHandler;



/**
 * 
 * @author kelly.li
 * @date 2015-07-16
 */
public class DubboDispatchServer extends AbstractDispatchServer {


	public DubboDispatchServer(int serverPort) {
		super(serverPort);
	}

	@Override
	public void initHandler(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("dubboDecoder", new DubboDecodeHandler());
		
	}

}
