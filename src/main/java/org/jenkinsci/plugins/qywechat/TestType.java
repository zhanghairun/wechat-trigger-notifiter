package org.jenkinsci.plugins.qywechat;

/**
 * 测试类型
 * 
 * @author zhanghairun
 */
public enum TestType {

	API("API"), //接口测试
	UI("UI"),   //UI测试
	PF("PF");  //性能测试

	private TestType(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	public String getDisplayName() {
		return displayName;
	}

}
