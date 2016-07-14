package qunar.tc.dubbocopy.balance;

import java.util.Random;

import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Target;

/**
 * 负载均衡随机
 * @author kelly.li
 * @date 2015-06-29
 */
public class RandomLoadBalance implements LoadBalance {

	private final Random random = new Random();

	public Target select(Group group) {
		int size = group.size();
		int index = random.nextInt(size);
		return group.get(index);
	}
}