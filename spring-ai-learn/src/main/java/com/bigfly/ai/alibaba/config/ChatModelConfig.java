package com.bigfly.ai.alibaba.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ChatModel 配置类，用于解决多个 ChatModel bean 的冲突
 */
@Configuration
public class ChatModelConfig {

    /**
     * 使用 @Primary 注解指定 dashScopeChatModel 为主要 ChatModel
     * 这样当有多个 ChatModel bean 时，Spring 会优先选择这个
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(DashScopeChatModel dashScopeChatModel) {
        return dashScopeChatModel;
    }
}