package org.jenkinsci.plugins.qywechat.model;

import org.jenkinsci.plugins.qywechat.ResultCondition;
import org.jenkinsci.plugins.qywechat.TestType;

import hudson.util.Secret;

/**
 * 配置项
 * 
 * @author zhanghairun
 */
public class NotificationConfig {

	/**
	 * 是否禁用通知
	 */
	public boolean disableWeChatNotifier = false;

	/**
	 * 测试类型
	 */
	public TestType testType = null;

	/**
	 * 构建结果状态
	 */
	public ResultCondition buildStatus = null;
	/**
	 * 企业微信WebHook地址
	 */
	public String weChatId = "";
	/**
	 * 通知用户ID
	 */
	public String mentionedId = "";

	/**
	 * 发送内容
	 */
	public String sendContent = "";

	/**
	 * 使用代理
	 */
	public boolean useProxy = false;
	/**
	 * 代理主机
	 */
	public String proxyHost = "";
	/**
	 * 代理端口
	 */
	public int proxyPort = 8080;
	/**
	 * 代理用户名
	 */
	public String proxyUsername = "";
	/**
	 * 代理密码
	 */
	public Secret proxyPassword = null;

	/**
	 * 构建编号
	 */
	public String buildNumber;

	/**
	 * 构建原因
	 */
	public String cause;
	/**
	 * 构建url
	 */
	public String buildUrl;
	/**
	 * joburl
	 */
	public String jobUrl;

}
