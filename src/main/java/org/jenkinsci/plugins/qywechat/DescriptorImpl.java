package org.jenkinsci.plugins.qywechat;

import java.util.logging.Logger;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;
import org.kohsuke.stapler.StaplerRequest;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.Secret;
import net.sf.json.JSONObject;

/**
 * 数据绑定
 * 
 * @author zhanghairun
 */
@Symbol("qyWechatNotification")
public class DescriptorImpl extends BuildStepDescriptor<Publisher> {

	private static final Logger logger = Logger.getLogger(DescriptorImpl.class.getName());

	private NotificationConfig config = new NotificationConfig();

	public DescriptorImpl() {
		super(QyWechatNotification.class);
		load();
	}

	public boolean isDisableWeChatNotifier() {
		return config.disableWeChatNotifier;
	}

	public void setDisableWeChatNotifier(boolean disableWeChatNotifier) {
		config.disableWeChatNotifier = disableWeChatNotifier;
	}

	public TestType getTestType() {
		return config.testType;
	}

	public void setTestType(TestType testType) {
		config.testType = testType;
	}

	public ResultCondition getBuildStatus() {
		return config.buildStatus;
	}

	public void setBuildStatus(ResultCondition buildStatus) {
		config.buildStatus = buildStatus;
	}

	public void setSendContent(String sendContent) {
		config.sendContent = sendContent;
	}

	public String getWeChatId() {
		return config.weChatId;
	}

	public void setWeChatId(String weChatId) {
		config.weChatId = weChatId;
	}

	public String getSendContent() {
		return config.sendContent;
	}

	public String getMentionedId() {
		return config.mentionedId;
	}

	public void setMentionedId(String mentionedId) {
		config.mentionedId = mentionedId;
	}

	public boolean isUseProxy() {
		return config.useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		config.useProxy = useProxy;
	}

	public String getProxyHost() {
		return config.proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		config.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return config.proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		config.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return config.proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		config.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return Secret.toString(config.proxyPassword);
	}

	public void setProxyPassword(String proxyPassword) {
		config.proxyPassword = Secret.fromString(proxyPassword);
	}

	/**
	 * 获取配置，不用于保存
	 * 
	 * @return
	 */
	public NotificationConfig getUnsaveConfig() {
		NotificationConfig unsaveConfig = new NotificationConfig();

		unsaveConfig.disableWeChatNotifier = config.disableWeChatNotifier;
		unsaveConfig.testType = config.testType;
		unsaveConfig.buildStatus = config.buildStatus;
		unsaveConfig.weChatId = config.weChatId;
		unsaveConfig.mentionedId = config.mentionedId;
		unsaveConfig.sendContent = config.sendContent;

		unsaveConfig.useProxy = config.useProxy;
		unsaveConfig.proxyHost = config.proxyHost;
		unsaveConfig.proxyPort = config.proxyPort;
		unsaveConfig.proxyUsername = config.proxyUsername;
		unsaveConfig.proxyPassword = config.proxyPassword;

		return unsaveConfig;
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> aClass) {
		return true;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
		config.disableWeChatNotifier = json.getBoolean("disableWeChatNotifier");
		config.testType = (TestType) json.get("testType");
		config.buildStatus = (ResultCondition) json.get("buildStatus");
		config.weChatId = json.getString("weChatId");
		config.mentionedId = json.getString("mentionedId");
		config.sendContent = json.getString("sendContent");
		config.useProxy = json.get("useProxy") != null;
		if (config.useProxy && json.get("useProxy") instanceof JSONObject) {
			JSONObject jsonObject = json.getJSONObject("useProxy");
			config.proxyHost = jsonObject.getString("proxyHost");
			config.proxyPort = jsonObject.getInt("proxyPort");
			config.proxyUsername = jsonObject.getString("proxyUsername");
			config.proxyPassword = Secret.fromString(jsonObject.getString("proxyPassword"));
		}
		save();
		return super.configure(req, json);
	}

	@Override
	public String getDisplayName() {
		return "企业微信通知";
	}

	/*
	 * @Override public String getHelpFile() { return
	 * "/plugin/qy-wechat-notification/help.html"; }
	 */
}
