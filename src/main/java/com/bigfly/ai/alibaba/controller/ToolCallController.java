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
     * 指定工具进行对话
     * GET /ali/tool/chat/specific?message=计算&tools=calculatorTools
     * 
     * 可用工具名称：
     * - timeTools: 时间相关工具
     * - calculatorTools: 计算器工具
     * - weatherTools: 天气查询工具
     */
    @GetMapping("/chat/specific")
    public String chatWithSpecificTools(
            @RequestParam String message,
            @RequestParam(required = false) String[] tools) {
        return toolCallService.chatWithSpecificTools(message, tools);
    }

    /**
     * 测试时间工具
     * GET /ali/tool/test/time
     */
    @GetMapping("/test/time")
    public String testTimeTools() {
        return toolCallService.chatWithSpecificTools("现在是什么时间？", "timeTools");
    }

    /**
     * 测试计算器工具
     * GET /ali/tool/test/calculator
     */
    @GetMapping("/test/calculator")
    public String testCalculatorTools() {
        return toolCallService.chatWithSpecificTools("计算 123 + 456 的结果", "calculatorTools");
    }

    /**
     * 测试天气工具
     * GET /ali/tool/test/weather
     */
    @GetMapping("/test/weather")
    public String testWeatherTools() {
        return toolCallService.chatWithSpecificTools("北京今天的天气怎么样？", "weatherTools");
    }

    /**
     * 综合测试（使用所有工具）
     * GET /ali/tool/test/all
     */
    @GetMapping("/test/all")
    public String testAllTools() {
        return toolCallService.chatWithTools("现在是几点？北京天气如何？计算 100 除以 3 的结果");
    }
}
