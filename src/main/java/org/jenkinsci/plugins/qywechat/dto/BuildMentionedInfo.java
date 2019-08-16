package org.jenkinsci.plugins.qywechat.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;

import hudson.model.Run;
import net.sf.json.JSONObject;

/**
 * `@`用户的通知
 * 
 * @author zhanghairun
 */
public class BuildMentionedInfo {

	/**
	 * 通知ID
	 */
	private String mentionedId = "";

	public BuildMentionedInfo(Run<?, ?> run, NotificationConfig config) {
		// 通知ID
		if (config.mentionedId != null) {
			mentionedId = config.mentionedId;
		}
	}

	public String toJSONString() {
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
		text.put("mentioned_list", mentionedIdList);

		Map data = new HashMap<String, Object>();
		data.put("msgtype", "text");
		data.put("text", text);

		String req = JSONObject.fromObject(data).toString();
		return req;
	}

}
