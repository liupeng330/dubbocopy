/*
 * Copyright (c) 2012 Qunar.com. All Rights Reserved.
 */
package qunar.tc.dubbocopy.router;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ObjectUtils;

import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Router;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.util.KeyGenerator;
import qunar.tc.dubbocopy.util.PrefixMatcher;

import com.google.common.collect.Maps;
import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

/**
 * @author kelly.li
 * @date 2015-06-29
 */
public class RouterSelector {

	private final PrefixMatcher<ConcurrentMap<String, Group>> tree = new PrefixMatcher<ConcurrentMap<String, Group>>(new DefaultCharArrayNodeFactory());

	public void addRouter(Router router) {
		String requestKey = router.getKey();
		for (Group group : router.getGroups()) {
			addGroup(router.getKey(), group);
			for (Target target : group) {
				addTarget(requestKey, group, target);
			}
		}
	}

	private Group addGroup(String service, Group group) {
		String groupName = group.getName();
		ConcurrentMap<String, Group> map = tree.getValueForExactKey(service);
		if (map == null) {
			map = new ConcurrentHashMap<String, Group>();
			map = ObjectUtils.defaultIfNull(tree.putIfAbsent(service, map), map);
		}

		Group cachedGroup = map.get(groupName);
		if (cachedGroup == null) {
			cachedGroup = ObjectUtils.defaultIfNull(map.putIfAbsent(groupName, group), group);
		}
		return cachedGroup;
	}

	private void addTarget(String service, Group group, Target target) {
		group = addGroup(service, group);
		group.add(target);
	}

	public void removeService(String service) {
		tree.remove(service);
	}


	public Map<String, Collection<Group>> select(String service, String method) {
		Map<String, Collection<Group>> map = Maps.newHashMap();
		String key = KeyGenerator.generateKey(service, method);
		for (KeyValuePair<ConcurrentMap<String, Group>> entry : tree.getKeyValuePairsForKeysPrefixIn(key)) {
			String entryKey = entry.getKey().toString();
			if (entryKey.equals(service) || entryKey.equals(key)) {
				map.put(entryKey, entry.getValue().values());
			}
		}
		return map;
	}

}
