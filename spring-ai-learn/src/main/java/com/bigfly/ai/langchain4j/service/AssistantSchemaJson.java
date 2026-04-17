package com.bigfly.ai.langchain4j.service;

import com.bigfly.ai.langchain4j.util.HistoryEvent;


public interface AssistantSchemaJson {
    /**
     * 最简单的对话，只返回助手的回答，不包含任何额外信息
     *
     * @param userMessage 用户消息
     * @return 助手生成的回答
     */
    HistoryEvent simpleChat(String userMessage);

}
