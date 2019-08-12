package com.plugins.wechattrigger;

import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

/**
 * Created by Marvin on 16/8/25.
 */
public class WeChatNotifier extends Notifier {

	private boolean disableWeChatNotifier; // 是否禁用通知插件
	private final ResultCondition buildStatus; // 构建结果状态
	private String wechatId; // 企业微信机器人ID
	private String sendUsers; // 发送人员,发送多人，请用英文逗号隔开
	private String sendContent; // 发送内容

	public boolean isDisableWeChatNotifier() {
		return disableWeChatNotifier;
	}

	public ResultCondition getBuildStatus() {
		return buildStatus;
	}

	public String getWechatId() {
		return wechatId;
	}

	public String getSendUsers() {
		return sendUsers;
	}

	public String getSendContent() {
		return sendContent;
	}

	// 绑定数据
	@DataBoundConstructor
	public WeChatNotifier(boolean disableWeChatNotifier, ResultCondition buildStatus, String wechatId, String sendUsers, String sendContent, boolean onStart, boolean onSuccess, boolean onFailed, boolean onAbort) {
		super();
		this.disableWeChatNotifier = disableWeChatNotifier;
		this.buildStatus = buildStatus;
		this.wechatId = wechatId;
		this.sendUsers = sendUsers;
		this.sendContent = sendContent;
	}

	/*
	 * public WeChatService newWeChatService(AbstractBuild build, TaskListener
	 * listener) { return new WeChatServiceImpl(disableWeChatNotifier, buildStatus,
	 * wechatId, sendUsers, sendContent, listener, build); }
	 */
	public WeChatService newWeChatService(Run<?, ?> build, TaskListener listener) {
		return new WeChatServiceImpl(disableWeChatNotifier, buildStatus, wechatId, sendUsers, sendContent, listener, build);
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		return true;
	}


	@Override
	public DingdingNotifierDescriptor getDescriptor() {
		return (DingdingNotifierDescriptor) super.getDescriptor();
	}

	@Extension
	public static class DingdingNotifierDescriptor extends BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		// 插件名称
		@Override
		public String getDisplayName() {
			return "WeChat Trigger Notifier";
		}

		// 设置默认值
		public ResultCondition getDefaultBuildStatus() {
			return ResultCondition.SUCCESS;
		}

	}
}
