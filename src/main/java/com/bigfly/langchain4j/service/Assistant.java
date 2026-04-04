package com.bigfly.langchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {
    /**
     * 最简单的对话，只返回助手的回答，不包含任何额外信息
     *
     * @param userMessage 用户消息
     * @return 助手生成的回答
     */
    String simpleChat(String userMessage);

    /**
     * 使用模板进行对话，返回助手的回答
     *
     * @param name 模板中的变量
     * @return 助手生成的回答
     */
    @UserMessage("简单介绍一下{{name}}")
    String temlateChat(@V("name") String name);

    @SystemMessage("你的回答不会超过一百汉字")
    @UserMessage("简单介绍一下{{name}}")
    String temlateChatWithSysMsg(@V("name") String name);

    /**
     * 带记忆的对话，返回助手的回答
     *
     * @param memoryId    记忆ID
     * @param userMessage 用户消息
     * @return 助手生成的回答
     */
    String chatByMemoryId(@MemoryId int memoryId, @UserMessage String userMessage);

    /**
     * 通过提示词range大模型返回JSON格式的内容
     *
     * @param userMessage 用户消息
     * @return 助手生成的回答
     */
    String byPrompt(@MemoryId int memoryId,@UserMessage String userMessage);

}
