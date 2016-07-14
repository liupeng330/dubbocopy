package qunar.tc.dubbocopy.util;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author sen.chai 14-12-2 上午11:51
 */
public class GlobalConfig {

	private static final Logger logger = LoggerFactory.getLogger(GlobalConfig.class);

	private List<String> configFiles;

	private boolean isFirstLoadConfig;

	public void setConfigFiles(List<String> configFiles) {
		this.configFiles = configFiles;
	}

	private static final ConcurrentMap<String, String> allConfig = Maps.newConcurrentMap();

	public void init() {
		isFirstLoadConfig = true;
		logger.info("global config loading begin");
		loadConfig();
		logger.info("global config loading finish");
		isFirstLoadConfig = false;
	}

	private void loadConfig() {
		Configuration.ConfigListener<Map<String, String>> mapConfigListener = new Configuration.ConfigListener<Map<String, String>>() {
			public void onLoad(Map<String, String> conf) {
				logger.info("detected config change...");
				// 应用启动时不执行
				if (!isFirstLoadConfig) {
					addConfig(conf);
				}
			}
		};
		for (String fileName : configFiles) {
			MapConfig mapConfig = MapConfig.get(fileName);
			mapConfig.addListener(mapConfigListener);
			Map<String, String> configMap = mapConfig.asMap();
			addConfig(configMap);
		}
	}

	/**
	 * Add global config
	 * 
	 * @param configMap
	 */
	private void addConfig(Map<String, String> configMap) {
		if (configMap == null) {
			return;
		}
		// 应用启动时进行检查
		if (isFirstLoadConfig) {
			checkConfig(configMap);
		}
		allConfig.putAll(configMap);
	}

	/**
	 * 启动时进行检查 Duplicate key check
	 * 
	 * @param configMap
	 */
	private void checkConfig(Map<String, String> configMap) {
		Set<String> configKeys = configMap.keySet();
		for (String configKey : configKeys) {
			if (allConfig.containsKey(configKey)) {
				logger.error("配置中包含重复的key:{}", configKey);
				throw new RuntimeException("配置中包含重复的key:" + configKey);
			}
		}
	}

	public static String get(String key) {
		String value = allConfig.get(key);
		if (value != null) {
			return value;
		}
		logger.error("没有找到配置，key:{}", key);
		throw new RuntimeException("没有找到配置，key:" + key);
	}

	public static int getInt(String key) {
		return Integer.parseInt(get(key).trim());
	}

	public static long getLong(String key) {
		return Long.parseLong(get(key).trim());
	}

	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key).trim());
	}

}
