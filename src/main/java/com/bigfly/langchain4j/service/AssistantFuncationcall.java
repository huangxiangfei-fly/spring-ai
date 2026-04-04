package com.bigfly.langchain4j.service;

import com.bigfly.langchain4j.util.HistoryEvent;
import dev.langchain4j.service.UserMessage;



public interface AssistantFuncationcall {
    /**
     * 通过提示词range大模型返回JSON格式的内容
     *
     * @param userMessage 用户消息
     * @return 助手生成的HistoryEvent对象
     */
    HistoryEvent byFunctionCall(@UserMessage String userMessage);
}
