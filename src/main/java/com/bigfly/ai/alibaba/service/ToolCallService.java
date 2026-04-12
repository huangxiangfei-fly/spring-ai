package com.bigfly.ai.alibaba.service;

import com.bigfly.ai.alibaba.tools.BaseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工具函数调用服务
 * 封装 AI 模型的 Function Calling 能力
 */
@Slf4j
@Service
public class ToolCallService {

    private final ChatClient chatClient;
    private final List<BaseTools> baseToolsList;

    public ToolCallService(ChatModel chatModel, List<BaseTools> baseToolsList) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.baseToolsList = baseToolsList;
    }

    /**
     * 使用工具进行对话（自动选择合适的工具）
     *
     * @param message 用户消息
     * @return AI 回复
     */
    public String chatWithTools(String message) {
        log.info("收到工具调用请求: {}", message);
        
        try {
            // 将所有 BaseTools 的工具实例传入
            Object[] toolObjects = baseToolsList.stream()
                    .map(BaseTools::getToolInstance)
                    .toArray();
            
            var prompt = chatClient.prompt()
                    .user(message)
                    .tools(toolObjects);  // 传入工具对象数组
            
            String response = prompt.call().content();
            
            log.info("工具调用完成，返回结果");
            return response;
        } catch (Exception e) {
            log.error("工具调用失败", e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }

    /**
     * 流式工具调用
     *
     * @param message 用户消息
     * @return 流式响应
     */
    public String streamChatWithTools(String message) {
        log.info("收到流式工具调用请求: {}", message);
        
        try {
            Object[] toolObjects = baseToolsList.stream()
                    .map(BaseTools::getToolInstance)
                    .toArray();
            
            StringBuilder result = new StringBuilder();
            chatClient.prompt()
                    .user(message)
                    .tools(toolObjects)
                    .stream()
                    .content()
                    .doOnNext(chunk -> result.append(chunk))
                    .blockLast();
            
            log.info("流式工具调用完成");
            return result.toString();
        } catch (Exception e) {
            log.error("流式工具调用失败", e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }

}
