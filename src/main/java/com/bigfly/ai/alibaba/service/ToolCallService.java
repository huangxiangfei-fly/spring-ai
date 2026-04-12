package com.bigfly.ai.alibaba.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
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
    private final List<ToolCallbackProvider> toolCallbackProviders;

    public ToolCallService(ChatModel chatModel, List<ToolCallbackProvider> toolCallbackProviders) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.toolCallbackProviders = toolCallbackProviders;
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
            var prompt = chatClient.prompt()
                    .user(message)
                    .tools(toolCallbackProviders.toArray(new ToolCallbackProvider[0]));  // 传入所有 Tool
            
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
            StringBuilder result = new StringBuilder();
            chatClient.prompt()
                    .user(message)
                    .tools(toolCallbackProviders.toArray(new ToolCallbackProvider[0]))
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

    /**
     * 测试特定工具的调用
     *
     * @param message 用户消息
     * @param toolNames 指定使用的工具名称列表（可选）
     * @return AI 回复
     */
    public String chatWithSpecificTools(String message, String... toolNames) {
        log.info("收到指定工具调用请求: {}, 工具: {}", message, toolNames);
        
        try {
            var promptBuilder = chatClient.prompt()
                    .user(message);
            
            // 如果指定了工具名称，则只使用这些工具
            if (toolNames != null && toolNames.length > 0) {
                promptBuilder.tools(toolNames);
            } else {
                promptBuilder.tools(toolCallbackProviders.toArray(new ToolCallbackProvider[0]));  // 使用所有工具
            }
            
            String response = promptBuilder.call().content();
            log.info("指定工具调用完成");
            return response;
        } catch (Exception e) {
            log.error("指定工具调用失败", e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }
}
