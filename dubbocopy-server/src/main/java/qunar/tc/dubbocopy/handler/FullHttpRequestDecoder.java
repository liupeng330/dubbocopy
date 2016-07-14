package qunar.tc.dubbocopy.handler;

import static io.netty.handler.codec.http.HttpConstants.COLON;
import static io.netty.handler.codec.http.HttpConstants.CR;
import static io.netty.handler.codec.http.HttpConstants.LF;
import static io.netty.handler.codec.http.HttpConstants.SP;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qunar.tc.dubbocopy.request.Request;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;

/**
 * @author kelly.li
 * @date 2015-07-12
 */
public class FullHttpRequestDecoder extends MessageToMessageDecoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(FullHttpRequestDecoder.class);

    private static final char SLASH = '/';
    private static final byte[] CRLF = {CR, LF};
    private static final char QUESTION_MARK = '?';
    private static final byte[] HEADER_SEPERATOR = {COLON, SP};

    private static final Logger LOGGER = LoggerFactory.getLogger(FullHttpRequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf = null;
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            dispaly(fullHttpRequest);
            String host = extractHost(ctx, fullHttpRequest);
            buf = ctx.alloc().buffer();
            encodeInitialLine(fullHttpRequest, buf);
            encodeHttpHeaders(fullHttpRequest, buf);
            encodeContent(fullHttpRequest, buf);
            Request request = new Request(host, "", buf);
            out.add(request);
        }

    }

    private String extractHost(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
            if (!host.endsWith(".qunar.com")) {
                host = host + ".qunar.com";
            }
            return host;
        } catch (Throwable e) {
            logger.error("extract host failed", e);
            return HttpHeaders.getHost(request);
        }
    }

    private void dispaly(FullHttpRequest fullHttpRequest) {
        LOGGER.info("****decode full http request**********");
        LOGGER.info(fullHttpRequest.getMethod().name() + " " + fullHttpRequest.getUri() + " " + fullHttpRequest.getProtocolVersion().text());
        for (String headerName : fullHttpRequest.headers().names()) {
            LOGGER.info(headerName + " " + HttpHeaders.getHeader(fullHttpRequest, headerName));
        }
        LOGGER.info("*************************************");

    }

    private void encodeContent(HttpContent content, ByteBuf buf) {
        buf.writeBytes(content.content());
    }

    private void encodeHttpHeaders(HttpRequest request, ByteBuf buf) {
        HttpHeaders httpHeaders = request.headers();
        for (Entry<String, String> header : httpHeaders) {
            encodeAscii0(header.getKey(), buf);
            buf.writeBytes(HEADER_SEPERATOR);
            encodeAscii0(header.getValue(), buf);
            buf.writeBytes(CRLF);
        }
        buf.writeBytes(CRLF);
    }

    private void encodeInitialLine(HttpRequest request, ByteBuf buf) throws Exception {
        encodeMethod(request.getMethod().name(), buf);
        buf.writeByte(SP);

        // Add / as absolute path if no is present.
        // See http://tools.ietf.org/html/rfc2616#section-5.1.2
        String uri = request.getUri();

        if (uri.length() == 0) {
            uri += SLASH;
        } else {
            int start = uri.indexOf("://");
            if (start != -1 && uri.charAt(0) != SLASH) {
                int startIndex = start + 3;
                // Correctly handle query params.
                // See https://github.com/netty/netty/issues/2732
                int index = uri.indexOf(QUESTION_MARK, startIndex);
                if (index == -1) {
                    if (uri.lastIndexOf(SLASH) <= startIndex) {
                        uri += SLASH;
                    }
                } else {
                    if (uri.lastIndexOf(SLASH, index) <= startIndex) {
                        int len = uri.length();
                        StringBuilder sb = new StringBuilder(len + 1);
                        sb.append(uri, 0, index);
                        sb.append(SLASH);
                        sb.append(uri, index, len);
                        uri = sb.toString();
                    }
                }
            }
        }

        buf.writeBytes(uri.getBytes(CharsetUtil.UTF_8));

        buf.writeByte(SP);
        encodeProtocolVersion(request.getProtocolVersion().text(), buf);
        buf.writeBytes(CRLF);
    }

    private void encodeMethod(String method, ByteBuf buf) {
        encodeAscii0(method, buf);
    }

    private void encodeProtocolVersion(String protocolVersion, ByteBuf buf) {
        encodeAscii0(protocolVersion, buf);
    }

    private void encodeAscii0(CharSequence seq, ByteBuf buf) {
        int length = seq.length();
        for (int i = 0; i < length; i++) {
            buf.writeByte((byte) seq.charAt(i));
        }
    }

}
