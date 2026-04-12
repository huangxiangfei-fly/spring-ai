package com.bigfly.ai.alibaba.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bigfly.ai.alibaba.service.ToolCallService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 工具函数调用控制器
 * 提供 Function Calling 相关的 HTTP 接口
 */
@RestController
@RequestMapping("/ali/tool")
@SaIgnore
public class ToolCallController {

    private final ToolCallService toolCallService;

    public ToolCallController(ToolCallService toolCallService) {
        this.toolCallService = toolCallService;
    }

    /**
     * 使用工具进行对话（自动选择工具）
     * GET /ali/tool/chat?message=今天北京天气怎么样
     * 
     * 示例问题：
     * - 今天北京天气怎么样？
     * - 1234 除以 56 等于多少？
     * - 现在几点了？
     * - 计算 25 和 75 的平均值
     */
    @GetMapping("/chat")
    public String chatWithTools(@RequestParam String message) {
        return toolCallService.chatWithTools(message);
    }

    /**
     * 流式工具调用
     * GET /ali/tool/chat/stream?message=帮我计算一下
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChatWithTools(@RequestParam String message) {
        return Flux.just(toolCallService.streamChatWithTools(message));
    }



    /**
     * 综合测试（使用所有工具）
     * GET /ali/tool/test/all
     */
    @GetMapping("/test/all")
    public String testAllTools() {
        return toolCallService.chatWithTools("现在是几点？北京天气如何？计算 100 除以 3 的结果");
    }

    /**
     * 测试 JSON 格式返回 - 获取用户信息
     * GET /ali/tool/test/json/user?userId=U001
     */
    @GetMapping("/test/json/user")
    public String testJsonUser(@RequestParam(defaultValue = "U001") String userId) {
        return toolCallService.chatWithSpecificTools(
                String.format("请获取用户 %s 的详细信息，并以 JSON 格式返回", userId), 
                new String[]{"jsonResponseTool"}  // Bean 名称：类名首字母小写
        );
    }

    /**
     * 测试 JSON 格式返回 - 获取订单信息
     * GET /ali/tool/test/json/order?orderId=ORD2024001
     */
    @GetMapping("/test/json/order")
    public String testJsonOrder(@RequestParam(defaultValue = "ORD2024001") String orderId) {
        return toolCallService.chatWithSpecificTools(
                String.format("请获取订单 %s 的详细信息，并以 JSON 格式返回", orderId), 
                new String[]{"jsonResponseTool"}
        );
    }

    /**
     * 测试 JSON 格式返回 - 获取用户列表
     * GET /ali/tool/test/json/users
     */
    @GetMapping("/test/json/users")
    public String testJsonUsers() {
        return toolCallService.chatWithSpecificTools(
                "请获取所有用户的列表信息，并以 JSON 数组格式返回", 
                new String[]{"jsonResponseTool"}
        );
    }
}
