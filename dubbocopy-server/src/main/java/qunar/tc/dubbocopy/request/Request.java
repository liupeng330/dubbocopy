package qunar.tc.dubbocopy.request;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by zhaohui.yu
 * 4/28/15
 */
public class Request {
    private final String serviceName;

    private final String methodName;

    private final ByteBuf rawData;

    public Request(String serviceName, String methodName, ByteBuf rawData) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.rawData = rawData;
        this.rawData.retain();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public ByteBuf getRawData() {
        return rawData;
    }

    public void release() {
        ReferenceCountUtil.safeRelease(this.rawData);
    }
}
