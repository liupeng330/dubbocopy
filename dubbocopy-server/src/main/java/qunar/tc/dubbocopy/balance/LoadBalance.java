package qunar.tc.dubbocopy.balance;

import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Target;

/**
 * 负载均衡
 * @author kelly.li
 * @date 2015-06-29
 */
public interface LoadBalance {

	Target select(Group group);
}
