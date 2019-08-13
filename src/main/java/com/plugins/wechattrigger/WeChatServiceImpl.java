package com.plugins.wechattrigger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

public class WeChatServiceImpl extends AbstractBuildParameters implements WeChatService {

	private Logger logger = LoggerFactory.getLogger(WeChatService.class);

	private boolean disableWeChatNotifier;
	private final ResultCondition buildStatus;
	private String wechatId;
	private String sendUsers;
	private String sendContent;
	private TaskListener listener;
	// private AbstractBuild build;
	private Run<?, ?> build;

	private static final String apiUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";
	private String api;

	public WeChatServiceImpl(boolean disableWeChatNotifier, ResultCondition buildStatus, String wechatId, String sendUsers, String sendContent, TaskListener listener, Run<?, ?> build) {
		this.disableWeChatNotifier = disableWeChatNotifier;
		this.buildStatus = buildStatus;
		this.wechatId = wechatId;
		this.sendUsers = sendUsers;
		this.sendContent = sendContent;
		this.listener = listener;
		this.build = build;
		this.api = apiUrl + wechatId;
	}

	@Override
	public void start() {
		// 工程构建开始

	}

	@Override
	public void sendMessage() {

		if (!disableWeChatNotifier) {
			logger.info("企业微信消息已发送");
			if (buildStatus.isMet(build.getResult(), build, listener)) {
				try {
					String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + wechatId;
					List<String> userlist = Arrays.asList(sendUsers.split(","));
					Map<String, Object> mapData = new HashMap<String, Object>();
					Map<String, Object> mapText = new HashMap<String, Object>();
					String content = getAction(build, listener, sendContent);

					mapText.put("mentioned_list", userlist);
					mapText.put("content", content);
					mapData.put("msgtype", "text");
					mapData.put("text", mapText);
					JSONObject obj = JSONObject.parseObject(JSON.toJSONString(mapData));
					// Object result2 = HttpClientService.sendPost(url, obj);
					// logger.info("企业微信消息发送结果:" + result2);
				} catch (Exception e) {
					logger.info(e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			logger.info("企业微信消息已禁止发送");
		}

	}

	@Override
	public void abort() {
		// 工程构建跳过
	}

	private String getBuildUrl() {
		String jenkinsURL = "";
		Jenkins instance = Jenkins.getInstance();
		assert instance != null;
		if (instance.getRootUrl() != null) {
			jenkinsURL = instance.getRootUrl();
		}

		if (jenkinsURL.endsWith("/")) {
			return jenkinsURL + build.getUrl();
		} else {
			return jenkinsURL + "/" + build.getUrl();
		}
	}

	@Override
	public String getAction(Run<?, ?> build, TaskListener listener, String sendContent) throws IOException, InterruptedException {
		EnvVars env = getEnvironment(build, listener);
		Set<Entry<String, String>> sets = env.entrySet();
		String content = RegexParameters.renderString(sendContent, sets);
		for (Entry<String, String> entry : sets) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		System.out.println("企业微信消息发送结果:" + content);
		return content;
	}

}
