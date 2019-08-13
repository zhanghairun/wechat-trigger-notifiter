package com.plugins.wechattrigger;

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParameters {

	// 匹配${XXXX}参数
	public static String renderString(String content, Set<Entry<String, String>> sets) {
		// Set<Entry<String, String>> sets = map.entrySet();
		for (Entry<String, String> entry : sets) {
			String regex = "\\$\\{" + entry.getKey() + "\\}";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content);
			content = matcher.replaceAll(entry.getValue());
		}
		return content;
	}

	// 替换参数内容
	/*
	 * public static void renderString() { String content =
	 * "说明：${name},说明：${six},说明：${Status}"; Map<String, String> map = new
	 * HashMap<>(); map.put("name", "java"); map.put("six", "6"); content =
	 * renderString(content, map); System.out.println(content); }
	 */
}