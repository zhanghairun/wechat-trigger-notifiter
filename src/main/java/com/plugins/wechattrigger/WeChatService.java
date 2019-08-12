package com.plugins.wechattrigger;

/**
 * Created by Marvin on 16/10/8.
 */
public interface WeChatService {
	void start();  //工程构建开始
	void abort();  //构建跳过
	void sendMessage();  //工程构建结束
} 
