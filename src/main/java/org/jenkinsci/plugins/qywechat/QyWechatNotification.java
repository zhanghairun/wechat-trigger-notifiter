package org.jenkinsci.plugins.qywechat;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.qywechat.dto.BuildOverInfo;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.arronlong.httpclientutil.exception.HttpProcessException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;

/**
 * 企业微信构建通知
 * 
 * @author zhanghairun
 */
//public class QyWechatNotification extends Publisher implements SimpleBuildStep {
public class QyWechatNotification extends Notifier implements SimpleBuildStep {

	private boolean disableWeChatNotifier; // 是否禁用通知插件

	private TestType testType; // 测试类型

	private ResultCondition buildStatus; // 构建结果状态

	private String weChatId;

	private String mentionedId;

	private String sendContent;

	private String projectName;

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public QyWechatNotification() {
	}

	/**
	 * 开始执行构建
	 * 
	 * @param build
	 * @param listener
	 * @return
	 */
	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		/*
		 * EnvVars envVars; try { envVars = build.getEnvironment(listener); } catch
		 * (Exception e) { listener.getLogger().println("读取环境变量异常" + e.getMessage());
		 * envVars = new EnvVars(); } NotificationConfig config = getConfig(envVars);
		 * if(StringUtils.isEmpty(config.webhookUrl)){ return true; } this.projectName =
		 * build.getProject().getFullDisplayName(); BuildBeginInfo buildInfo = new
		 * BuildBeginInfo(this.projectName, build, config);
		 * 
		 * String req = buildInfo.toJSONString(); listener.getLogger().println("推送通知" +
		 * req);
		 * 
		 * //执行推送 push(listener.getLogger(), config.webhookUrl, req, config);
		 */
		return true;
	}

	/**
	 * 构建结束
	 * 
	 * @param run
	 * @param workspace
	 * @param launcher
	 * @param listener
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

		NotificationConfig config = getConfig(run, launcher, listener);

		// 判断是否禁用此插件
		if (config.disableWeChatNotifier) {
			return;
		}

		// 判断微信ID是否为空
		if (StringUtils.isEmpty((config.weChatId).trim())) {
			return;
		}

		// 判断发送的人员信息是否为空
		if (StringUtils.isEmpty((config.mentionedId).trim())) {
			return;
		}

		Result result = run.getResult();
		// 运行不成功
		if (result == null) {
			return;
		}

		if (buildStatus.isMet(result, run, listener)) {
			// 设置当前项目名称
			if (run instanceof AbstractBuild) {
				this.projectName = run.getParent().getFullDisplayName();
			}

			// 构建结束通知
			BuildOverInfo buildInfo = new BuildOverInfo(run, launcher, listener, config);
			String req = buildInfo.toJSONString();

			// 项目运行推送通知
			listener.getLogger().println("发送企业微信通知！");
			// 推送通知内容
			listener.getLogger().println("推送通知内容：" + req);
			// 执行推送
			push(listener.getLogger(), config.weChatId, req, config);
		}
	}

	/**
	 * 推送消息
	 * 
	 * @param logger
	 * @param weChatId
	 * @param data
	 * @param config
	 */
	private void push(PrintStream logger, String weChatId, String data, NotificationConfig config) {
		String[] weChatIds;
		if (weChatId.contains(",")) {
			weChatIds = weChatId.split(",");
		} else {
			weChatIds = new String[] { weChatId };
		}
		for (String weId : weChatIds) {
			try {
				String msg = NotificationUtil.push(weId, data, config);
				logger.println("推送通知结果：" + msg);
			} catch (HttpProcessException e) {
				logger.println("推送通知异常：" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	/**
	 * 读取配置，将当前Job与全局配置整合
	 * 
	 * @param envVars
	 * @return
	 */
	public NotificationConfig getConfig(Run<?, ?> run, Launcher launcher, TaskListener listener) {
		NotificationConfig config = DESCRIPTOR.getUnsaveConfig();
		config.disableWeChatNotifier = disableWeChatNotifier;
		config.testType = testType;
		config.buildStatus = buildStatus;
		config.weChatId = weChatId;
		config.mentionedId = mentionedId;
		config.sendContent = sendContent;
		return config;
	}

	/** 下面为GetSet方法，当前Job保存时进行绑定 **/

	@DataBoundSetter
	public void setDisableWeChatNotifier(boolean disableWeChatNotifier) {
		this.disableWeChatNotifier = disableWeChatNotifier;
	}

	@DataBoundSetter
	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	@DataBoundSetter
	public void setBuildStatus(ResultCondition buildStatus) {
		this.buildStatus = buildStatus;
	}

	@DataBoundSetter
	public void setWeChatId(String weChatId) {
		this.weChatId = weChatId;
	}

	@DataBoundSetter
	public void setMentionedId(String mentionedId) {
		this.mentionedId = mentionedId;
	}

	@DataBoundSetter
	public void setSendContent(String sendContent) {
		this.sendContent = sendContent;
	}

	public boolean isDisableWeChatNotifier() {
		return disableWeChatNotifier;
	}

	public TestType getTestType() {
		return testType;
	}

	public ResultCondition getBuildStatus() {
		return buildStatus;
	}

	public String getWeChatId() {
		return weChatId;
	}

	public String getMentionedId() {
		return mentionedId;
	}

	public String getSendContent() {
		return sendContent;
	}
}
