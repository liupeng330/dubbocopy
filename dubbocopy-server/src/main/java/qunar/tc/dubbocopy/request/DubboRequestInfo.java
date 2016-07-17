package qunar.tc.dubbocopy.request;


import org.apache.commons.lang3.StringUtils;

/**
 * @author song.xue created on 15/4/22
 * @version 1.0.0
 */
public class DubboRequestInfo {

	private final String serviceName;
	private final String methodName;

	public DubboRequestInfo(String serviceName, String methodName) {
		this.serviceName = serviceName;
		this.methodName = methodName;
	}

	public String getKey() {
		return serviceName + (StringUtils.isEmpty(methodName) ? "" : "_" + methodName);
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DubboRequestInfo that = (DubboRequestInfo) o;

		if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
			return false;
		if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = serviceName != null ? serviceName.hashCode() : 0;
		result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "DubboRequestInfo{" + "serviceName='" + serviceName + '\'' + ", methodName='" + methodName + '\'' + '}';
	}
}
