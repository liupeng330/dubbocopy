package qunar.tc.dubbocopy.api.model;

import java.io.Serializable;
import java.util.Set;

/**
 * @author song.xue created on 15/4/22
 * @version 1.0.0
 */
public class Router implements Serializable {
	private static final long serialVersionUID = 3162565165149998433L;
	private String serviceName;
	private String methodName;
	private Set<Target> targets;
	private int n = 1;
	private Set<Group> groups;

	public Router() {
	}

	public Router(String serviceName, String methodName, Set<Target> targets) {
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.targets = targets;
	}

	public Router(String serviceName, String methodName, int n, Set<Group> groups) {
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.n = n;
		this.groups = groups;
		for (Group group : groups) {
			group.setN(n);
		}
	}

	public String getKey() {
		return serviceName + (methodName == null || methodName.trim().length() == 0 ? "" : "_" + methodName);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Set<Target> getTargets() {
		return targets;
	}

	public void setTargets(Set<Target> targets) {
		this.targets = targets;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	@Override
	public String toString() {
		return "Router{" + "serviceName='" + serviceName + '\'' + ", methodName='" + methodName + '\'' + ", n=" + n + ", groups=" + groups + '\'' + ", targets=" + targets + '}';
	}
}
