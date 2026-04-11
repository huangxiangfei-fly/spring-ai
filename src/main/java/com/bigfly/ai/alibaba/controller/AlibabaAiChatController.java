package com.bigfly.ai.alibaba.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 通义千问控制器，处理与大模型交互的HTTP请求
 */
@RestController
@RequestMapping("/ali/qwen")
public class AlibabaAiChatController {

    private final ChatClient chatClient;

    public AlibabaAiChatController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 简单对话接口
     * GET /ali/qwen/chat?message=你好
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式对话接口（SSE）
     * GET /ali/qwen/chat/stream?message=你好
     */
    @SaIgnore
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

}
