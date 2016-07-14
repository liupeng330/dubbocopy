package qunar.tc.dubbocopy.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.util.Monitor;

/**
 * @author song.xue created on 15/4/23
 * @version 1.0.0
 */
public class RawByteBufRequest implements RequestListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RawByteBufRequest.class);

	public static class Builder {
		private String serviceName;
		private String methodName;
		private String groupName;
		private int n;
		private Target target;
		private ByteBuf byteBuf;

		public Builder(String serviceName, String methodName, String groupName, int n, Target target, ByteBuf byteBuf) {
			this.serviceName = serviceName;
			this.methodName = methodName;
			this.groupName = groupName;
			this.n = n;
			this.target = target;
			this.byteBuf = byteBuf;
		}

		

		public RawByteBufRequest build() {
			return new RawByteBufRequest(serviceName, methodName, groupName, n, target, byteBuf);
		}
	}

	private String serviceName;
	private String methodName;
	private String groupName;
	private int n;
	private Target target;
	private ByteBuf byteBuf;

	private RawByteBufRequest(String serviceName, String methodName, String groupName, int n, Target target, ByteBuf byteBuf) {
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.groupName = groupName;
		this.n = n;
		this.target = target;
		this.byteBuf = byteBuf;
	}

	public Target getTarget() {
		return target;
	}

	public ByteBuf getByteBuf() {
		return byteBuf;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getGroupName() {
		return groupName;
	}

	public int getN() {
		return n;
	}

	@Override
	public String toString() {
		return "RawByteBufRequest{" + "serviceName=" + serviceName + "methodName=" + methodName + "groupName=" + groupName + "n=" + n + ", byteBuf=" + byteBuf + '}';
	}

	@Override
	public void onConnected() {
		LOGGER.info("连接成功  {}.{} ->{}->{}:{}", serviceName, methodName, groupName, target.getHost(), target.getPort());
		Monitor.connectSuccess.mark();
	}

	@Override
	public void onConnectFailed(Throwable e) {
		release();
		LOGGER.error("连接失败  {}.{} ->{}->{}:{}", serviceName, methodName, groupName, target.getHost(), target.getPort());
		Monitor.connectFail.mark();

	}

	@Override
	public void onUnWritable() {
		LOGGER.info("不可写  {}.{} ->{}->{}:{}", serviceName, methodName, groupName, target.getHost(), target.getPort());
		release();
	}

	@Override
	public void onWriteSuccess() {
		LOGGER.info("请求成功  {}.{} ->{}->{}:{}", serviceName, methodName, groupName, target.getHost(), target.getPort());
		Monitor.requestSuccess.mark();
	}

	@Override
	public void onWriteFailed(Throwable e) {
		LOGGER.error("请求失败  {}.{} ->{}->{}:{}", serviceName, methodName, groupName, target.getHost(), target.getPort(), e);
		Monitor.requestFail.mark();
	}

	public void release() {
		ReferenceCountUtil.safeRelease(byteBuf);
	}
}
