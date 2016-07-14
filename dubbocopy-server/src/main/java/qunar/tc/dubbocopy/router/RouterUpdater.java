package qunar.tc.dubbocopy.router;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.dubbocopy.api.model.Router;
import qunar.tc.dubbocopy.util.GlobalConfig;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author song.xue created on 15/4/28
 * @version 1.0.0
 */
public class RouterUpdater {

	private static final Logger LOGGER = LoggerFactory.getLogger(RouterUpdater.class);

	@Resource
	private AsyncHttpClient asyncHttpClient;

	@Resource
	private RouterService routerService;

	public void updateAll() {
		update(GlobalConfig.get("dubbocopy.cactusRouterUrl"));
		update(GlobalConfig.get("dubbocopy.appcenterRouterUrl"));
	}

	public void update(String url) {
		try {
			LOGGER.info("开始从 {} 拉取路由", url);
			RequestBuilder builder = new RequestBuilder("GET");
			builder.setUrl(url);
			Response response = asyncHttpClient.executeRequest(builder.build()).get();
			if (response.getStatusCode() != 200) {
				throw new IOException("从" + url + "抓取router失败");
			}
			String responseBody = response.getResponseBody();
			List<Router> routers = parseRouter(responseBody);
			routerService.refreshAllRouters(routers);
			LOGGER.info("拉取路由完成，共 {} 条路由", routers.size());
		} catch (Exception e) {
			LOGGER.error("拉取路由失败", e);
		}
	}

	private List<Router> parseRouter(String responseBody) throws IOException {
		Gson gson = new Gson();
		FetchRouterResult fetchRouterResult = gson.fromJson(responseBody, FetchRouterResult.class);
		if (fetchRouterResult.getStatus() != 0) {
			throw new IOException("路由接口返回错误，status = " + fetchRouterResult.getStatus() + ", message = " + fetchRouterResult.getMessage());
		}
		if (fetchRouterResult.getData() == null) {
			return Lists.newArrayList();
		}
		for (Router router : fetchRouterResult.getData()) {
			if (router.getMethodName() != null && router.getMethodName().equals("")) {
				router.setMethodName(null);
			}
		}
		return fetchRouterResult.getData();
	}

	private static class FetchRouterResult {
		private int status;
		private String message;
		private List<Router> data;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<Router> getData() {
			return data;
		}

		public void setData(List<Router> data) {
			this.data = data;
		}
	}
}
