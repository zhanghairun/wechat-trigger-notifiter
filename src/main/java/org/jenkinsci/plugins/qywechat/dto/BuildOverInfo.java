package org.jenkinsci.plugins.qywechat.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.qywechat.NotificationUtil;
import org.jenkinsci.plugins.qywechat.TestType;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;

import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;

/**
 * 结束构建的通知信息
 * 
 * @author zhanghairun
 */
public class BuildOverInfo {

	/**
	 * 通知ID
	 */
	private String mentionedId = "";

	/**
	 * 发送的内容
	 */
	private String sendContent = "";

	/**
	 * 测试类型
	 */
	public TestType testType;

	public BuildOverInfo(Run<?, ?> run, Launcher launcher, TaskListener listener, NotificationConfig config) {
		try {
			Map<String, String> envMap = NotificationUtil.getContentEnvValue(run, launcher, listener);

			// 测试类型
			if (config.testType != null) {
				testType = config.testType;
			}
			
			// 环境名称
			if (StringUtils.isNotEmpty((config.sendContent).trim())) {
				sendContent = NotificationUtil.replaceContentEnvValue(run, listener, (config.sendContent).trim(), envMap);
			} else {
				if (testType.getDisplayName().equals("API")) {
					sendContent = "项目名称：${PROJECT_NAME}" + "\n构建编号：${BUILD_NUMBER}" + "\n触发原因：${CAUSE}" + "\n运行结果：${BUILD_STATUS}" + "\n本次运行报告：${BUILD_URL}HTML_20Report" + "\n最新运行报告：${JOB_URL}HTML_20Report";
				} else if (testType.getDisplayName().equals("UI")) {
					sendContent = "项目名称：${PROJECT_NAME}" + "\n构建编号：${BUILD_NUMBER}" + "\n触发原因：${CAUSE}" + "\n运行结果：${BUILD_STATUS}" + "\n本次运行报告：${BUILD_URL}robot/report/log.html";
				} else if (testType.getDisplayName().equals("PF")) {
					sendContent = "项目名称：${PROJECT_NAME}" + "\n构建编号：${BUILD_NUMBER}" + "\n触发原因：${CAUSE}" + "\n运行结果：${BUILD_STATUS}" + "\n本次运行报告：${BUILD_URL}HTML_20Report";
				}
				sendContent = NotificationUtil.replaceContentEnvValue(run, listener, sendContent, envMap);
			}
			// 通知ID
			if (config.mentionedId != null) {
				mentionedId = config.mentionedId;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String toJSONString() {
		// 发送的人员信息
		List<String> mentionedIdList = new ArrayList<>();
		if (StringUtils.isNotEmpty(mentionedId)) {
			String[] ids = mentionedId.split(",");
			for (String id : ids) {
				if ("all".equals(id.toLowerCase())) {
					id = "@all";
				}
				mentionedIdList.add(id);
			}
		}

		Map text = new HashMap<String, Object>();
		text.put("content", sendContent);
		text.put("mentioned_list", mentionedIdList);

		Map data = new HashMap<String, Object>();
		data.put("msgtype", "text");
		data.put("text", text);

		String req = JSONObject.fromObject(data).toString();
		return req;
	}

}
