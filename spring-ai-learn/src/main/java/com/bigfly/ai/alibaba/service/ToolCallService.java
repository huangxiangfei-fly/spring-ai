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

    /**
     * 指定工具进行对话
     *
     * @param message 用户消息
     * @param toolNames 指定使用的工具名称列表（Bean 名称）
     * @return AI 回复
     */
    public String chatWithSpecificTools(String message, String... toolNames) {
        log.info("收到指定工具调用请求: {}, 工具: {}", message, toolNames);
        
        try {
            var promptBuilder = chatClient.prompt()
                    .user(message);
            
            // 如果指定了工具名称，则只使用这些工具
            if (toolNames != null && toolNames.length > 0) {
                // 根据工具名称查找对应的 BaseTools 实例
                Object[] selectedTools = baseToolsList.stream()
                        .filter(tool -> {
                            // 获取类名（去掉首字母大写）
                            String className = tool.getClass().getSimpleName();
                            String beanName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                            return java.util.Arrays.asList(toolNames).contains(beanName);
                        })
                        .map(BaseTools::getToolInstance)
                        .toArray();
                
                if (selectedTools.length > 0) {
                    promptBuilder.tools(selectedTools);
                } else {
                    log.warn("未找到指定的工具: {}", java.util.Arrays.toString(toolNames));
                    //  fallback: 使用所有工具
                    Object[] allTools = baseToolsList.stream()
                            .map(BaseTools::getToolInstance)
                            .toArray();
                    promptBuilder.tools(allTools);
                }
            } else {
                // 使用所有工具
                Object[] toolObjects = baseToolsList.stream()
                        .map(BaseTools::getToolInstance)
                        .toArray();
                promptBuilder.tools(toolObjects);
            }
            
            String response = promptBuilder.call().content();
            
            // 清理 Markdown 代码块标记
            response = cleanMarkdownJson(response);
            
            log.info("指定工具调用完成");
            return response;
        } catch (Exception e) {
            log.error("指定工具调用失败", e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }

    /**
     * 清理返回内容中的 Markdown JSON 代码块标记
     * 将 ```json ... ``` 或 ``` ... ``` 转换为纯 JSON
     *
     * @param content 原始内容
     * @return 清理后的内容
     */
    private String cleanMarkdownJson(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 移除开头的 ```json 或 ```
        content = content.replaceAll("^```json\s*", "");
        content = content.replaceAll("^```\s*", "");
        
        // 移除结尾的 ```
        content = content.replaceAll("\s*```$", "");
        
        // 去除首尾空白
        return content.trim();
    }
}
