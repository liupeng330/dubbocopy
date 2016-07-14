package qunar.tc.dubbocopy.router;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qunar.metrics.Metrics;
import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Router;
import qunar.tc.dubbocopy.request.DubboRequestInfo;
import qunar.tc.dubbocopy.util.Monitor;

import com.google.common.collect.Lists;

/**
 * @author song.xue created on 15/4/23
 * @version 1.0.0
 */
public class RouterServiceImpl implements RouterService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RouterServiceImpl.class);

	@Resource
	private RouterUpdater routerUpdater;
	@Resource
	private RouterSelector reouterSelector;

	public void refreshAllRouters(List<Router> routers) {
		LOGGER.info("refreshAllRouters");
		if (routers == null || routers.isEmpty()) {
			return;
		}
		for (Router router : routers) {
			setRouter(router);
		}
	}

	public void setRouter(Router router) {
		String key = router.getKey();
		if (router.getGroups() == null || router.getGroups().isEmpty()) {
			reouterSelector.removeService(key);
			LOGGER.info("remove router {}", router);
		} else {
			reouterSelector.addRouter(router);
			LOGGER.info("add router {}", router);
		}
		Monitor.routerChanged.mark();
	}

	@PostConstruct
	public void queryAllRoutersFromCactus() {
		routerUpdater.updateAll();
	}

	public List<Group> selectGroups(DubboRequestInfo requestInfo) {
		Map<String, Collection<Group>> groupMap = reouterSelector.select(requestInfo.getServiceName(), requestInfo.getMethodName());
		List<Group> list = Lists.newArrayList();
		for (Map.Entry<String, Collection<Group>> entry : groupMap.entrySet()) {
			for (Group group : entry.getValue()) {
				if (group.size() == 0) {
					LOGGER.info("没有router，不发送请求 {}", requestInfo);
					Metrics.meter(Monitor.REQUEST_NO_ROUTER).tag("service", requestInfo.getServiceName()).get().mark();
				} else {
					list.add(group);
				}
			}
		}
		return list;
	}

}
