package com.bigfly.langchain4j.service;

import com.bigfly.langchain4j.util.HistoryEvent;
import dev.langchain4j.service.UserMessage;


public interface AssistantFormJson {
    /**
     * 最简单的对话，只返回助手的回答，不包含任何额外信息
     *
     * @param userMessage 用户消息
     * @return 助手生成的回答
     */
    HistoryEvent simpleChat(String userMessage);

}
