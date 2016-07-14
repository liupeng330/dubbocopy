package qunar.tc.dubbocopy.handler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import qunar.tc.dubbocopy.request.Request;
import qunar.tc.dubbocopy.util.Monitor;

import java.util.List;

/**
 * @author song.xue created on 15/4/22
 * @version 1.0.0
 */
/*
Dubbo字节码结构
header 16 bytes
        magic 2bytes 固定是0xdabb
        一些位标记 1byte
            最高位 request为1,response为0
            第2高位 貌似是保留位
            第3高位 FLAG_TWOWAY
            第4高位 EVENT为1，非EVENT为0
            后4位 content type id,对于默认的Hessian2Serialization是2
        1byte 再看是啥
        request id 8bytes
        datalength 4bytes
data datalength bytes
        Hessian2String dubbo版本
        Hessian2String 服务名
        Hessian2String 服务版本
        Hessian2String 方法名
        后面的暂时不用管
*/
public class DubboDecodeHandler extends ByteToMessageDecoder {

    private static final int DUBBO_HEADER_LEN = 16;

    protected static final byte FLAG_REQUEST = (byte) 0x80;

    protected static final byte FLAG_EVENT = (byte) 0x20;

    protected static final byte HESSIAN_SERILIZATION_ID = 2;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Monitor.serverChannelRead.mark();
        if (in.readableBytes() < DUBBO_HEADER_LEN) {
            return;
        }
        in.markReaderIndex();
        byte[] header = new byte[DUBBO_HEADER_LEN];
        in.readBytes(header, 0, DUBBO_HEADER_LEN);
        int dataLen = getDataLenFromHeader(header);
        if (in.readableBytes() < dataLen) {
            in.resetReaderIndex();
            return;
        }
        if (!isDubboMagicValid(header)) {
            ctx.channel().close();
            Monitor.notDubboMagic.mark();
            return;
        }
        if (!isHessian2RequestFlag(header[2])) {
            Monitor.notDubboRequest.mark();
            in.skipBytes(dataLen);
            return;
        }
        Request request = parseRequestInfo(in, dataLen);
        out.add(request);
        Monitor.requestDecoded.mark();
    }

    private Request parseRequestInfo(ByteBuf data, int dataLen) {
        readString(data, true);//skip dubbo version
        String serviceName = readString(data, false);
        readString(data, true);//skip service version
        String methodName = readString(data, false);
        data.resetReaderIndex();
        ByteBuf rawData = data.readSlice(DUBBO_HEADER_LEN + dataLen);
        return new Request(serviceName, methodName, rawData);
    }

    private String readString(ByteBuf buf, boolean skip) {
        byte firstByte = buf.readByte();
        return readStringByFirstByte(firstByte, buf, skip);
    }

    private String readStringByFirstByte(byte firstByte, ByteBuf buf, boolean skip) {
        if (Hessian2Constants.BC_STRING_CHUNK == firstByte) {
            return readStringChunks(buf, skip);
        }
        if (Hessian2Constants.BC_STRING == firstByte) {
            int len = unsignedShort(buf.readShort());
            return readStringByLength(buf, len, skip);
        }
        if (firstByte > Hessian2Constants.STRING_DIRECT_MAX) {
            short len = Shorts.fromBytes((byte) (firstByte - Hessian2Constants.BC_STRING_SHORT), buf.readByte());
            return readStringByLength(buf, len, skip);
        }
        return readStringByLength(buf, firstByte, skip);
    }

    private String readStringByLength(ByteBuf buf, int length, boolean skip) {
        if (skip) {
            buf.skipBytes(length);
            return null;
        }
        byte[] stringBuf = new byte[length];
        buf.readBytes(stringBuf, 0, length);
        return new String(stringBuf);
    }

    private String readStringChunks(ByteBuf buf, boolean skip) {
        StringBuilder builder = skip ? null : new StringBuilder();
        byte nextChunkFirstByte;
        do {
            int len = unsignedShort(buf.readShort());
            if (skip) {
                readStringByLength(buf, len, true);
            } else {
                builder.append(readStringByLength(buf, len, false));
            }
            nextChunkFirstByte = buf.readByte();
        } while (nextChunkFirstByte == Hessian2Constants.BC_STRING_CHUNK);
        if (skip) {
            readStringByFirstByte(nextChunkFirstByte, buf, true);
            return null;
        }
        builder.append(readStringByFirstByte(nextChunkFirstByte, buf, false));
        return builder.toString();
    }

    private int unsignedShort(short val) {
        return val & 0xffff;
    }

    private boolean isHessian2RequestFlag(byte flag) {
        return ((flag & FLAG_REQUEST) != 0) && ((flag & FLAG_EVENT) == 0) && ((flag & (byte) 0x0f) == HESSIAN_SERILIZATION_ID);
    }

    private boolean isDubboMagicValid(byte[] header) {
        return header[0] == (byte) 0xda && header[1] == (byte) 0xbb;
    }

    private int getDataLenFromHeader(byte[] header) {
        return Ints.fromBytes(header[12], header[13], header[14], header[15]);
    }

}
