package org.jenkinsci.plugins.qywechat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.model.Jenkins;

/**
 * 工具类
 * 
 * @author zhanghairun
 */
public class NotificationUtil {

	/**
	 * 推送信息
	 * 
	 * @param weChatId
	 * @param data
	 */
	public static String push(String weChatId, String data, NotificationConfig buildConfig) throws HttpProcessException {
		HttpConfig httpConfig;
		String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + weChatId;
		// 使用代理请求
		if (buildConfig.useProxy) {
			HttpClient httpClient;
			HttpHost proxy = new HttpHost(buildConfig.proxyHost, buildConfig.proxyPort);
			// 用户密码
			if (StringUtils.isNotEmpty(buildConfig.proxyUsername) && buildConfig.proxyPassword != null) {
				// 设置认证
				CredentialsProvider provider = new BasicCredentialsProvider();
				provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(buildConfig.proxyUsername, Secret.toString(buildConfig.proxyPassword)));
				httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).setProxy(proxy).build();
			} else {
				httpClient = HttpClients.custom().setProxy(proxy).build();
			}
			// 代理请求

			httpConfig = HttpConfig.custom().client(httpClient).url(url).json(data).encoding("utf-8");
		} else {
			// 普通请求
			httpConfig = HttpConfig.custom().url(url).json(data).encoding("utf-8");
		}

		String result = HttpClientUtil.post(httpConfig);
		return result;
	}

	/**
	 * 获取Jenkins地址
	 * 
	 * @return
	 */
	public static String getJenkinsUrl() {
		String jenkinsUrl = Jenkins.getInstance().getRootUrl();
		if (jenkinsUrl != null && jenkinsUrl.length() > 0 && !jenkinsUrl.endsWith("/")) {
			jenkinsUrl = jenkinsUrl + "/";
		}
		return jenkinsUrl;
	}

	// 替换参数内容
	public static String replaceContentEnvValue(Run<?, ?> run, TaskListener listener, String sendContent, Map<String, String> map) throws IOException, InterruptedException {
		EnvVars env = run.getEnvironment(listener);
		Set<Entry<String, String>> sets = env.entrySet();
		sendContent = renderString(sendContent, sets);
		sendContent = renderString(sendContent, map);
		return sendContent;
	}

	// 匹配${XXXX}参数
	public static String renderString(String content, Set<Entry<String, String>> sets) {
		for (Entry<String, String> entry : sets) {
			String regex = "\\$\\{" + entry.getKey() + "\\}";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content);
			content = matcher.replaceAll(entry.getValue());
		}
		return content;
	}

	// 替换参数内容
	public static String renderString(String content, Map<String, String> map) {
		Set<Entry<String, String>> sets = map.entrySet();
		content = renderString(content, sets);
		return content;
	}

	// 获取指的定系统参数
	public static Map<String, String> getContentEnvValue(Run<?, ?> run, Launcher launcher, TaskListener listener) {
		try {
			Map<String, String> envMap = new HashMap<String, String>();

			String USE_TIME = run.getTimestampString();
			envMap.put("USE_TIME", USE_TIME);// 使用时间

			StringBuilder urlBuilder = new StringBuilder(); // 控制台地址
			String jenkinsUrl = NotificationUtil.getJenkinsUrl();
			if (StringUtils.isNotEmpty(jenkinsUrl)) {
				envMap.put("JENKINS_URL", jenkinsUrl);

				String buildUrl = run.getUrl();
				String jobUrl = run.getParent().getUrl();

				// 设置当前项目名称
				if (run instanceof AbstractBuild) {
					String PROJECT_NAME = run.getParent().getFullDisplayName();// PROJECT_NAME
					envMap.put("PROJECT_NAME", PROJECT_NAME);
				}

				int BUILD_NUMBER = run.getNumber(); // BUILD_NUMBER
				envMap.put("BUILD_NUMBER", BUILD_NUMBER + "");

				urlBuilder.append(jenkinsUrl);
				if (!jenkinsUrl.endsWith("/")) {
					urlBuilder.append("/");
				}
				String JOB_URL = jenkinsUrl.toString() + jobUrl; // JOB_URL
				envMap.put("JOB_URL", JOB_URL);

				urlBuilder.append(buildUrl);
				if (!buildUrl.endsWith("/")) {
					urlBuilder.append("/");
				}
				String BUILD_URL = urlBuilder.toString(); // BUILD_URL
				envMap.put("BUILD_URL", BUILD_URL);

				urlBuilder.append("console");
				envMap.put("CONSOLE_URL", urlBuilder.toString());// CONSOLE_URL 控制台地址
			}

			String CAUSE = getByUserIdCause(run);
			envMap.put("CAUSE", CAUSE); // CAUSE 获取构建原因

			// 结果
			Result BUILD_STATUS = run.getResult();
			envMap.put("BUILD_STATUS", BUILD_STATUS.toString()); // BUILD_STATUS 构建结果

			return envMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取构建原因
	 * 
	 * @param run
	 * @return
	 */
	private static String getByUserIdCause(Run<?, ?> run) {
		try {
			Cause.UserIdCause cause = run.getCause(Cause.UserIdCause.class);
			if (cause != null) {
				String id = cause.getUserId();
				String name = cause.getUserName();
				String description = cause.getShortDescription();
				return description;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
