package com.bigfly.langchain4j.controller;


import com.bigfly.langchain4j.service.QwenService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 通义千问控制器，处理与大模型交互的HTTP请求
 */
@RestController
@RequestMapping("/api/qwen")
public class QwenController {

    private final QwenService qwenService;

    /**
     * 构造函数，通过依赖注入获取QwenService实例
     *
     * @param qwenService QwenService实例
     */
    @Autowired
    public QwenController(QwenService qwenService) {
        this.qwenService = qwenService;
    }

    /**
     * 提示词请求实体类
     */
    @Data
    static class PromptRequest {
        private String prompt;
        private int userId;
        private int imageNum;
        private String imageUrl;
        private List<String> imageUrls;
    }

    /**
     * 响应实体类
     */
    @Data
    static class Response {
        private String result;

        public Response(String result) {
            this.result = result;
        }
    }

    /**
     * 检查请求体是否有效
     *
     * @param request 包含提示词的请求体
     * @return 如果有效则返回null，否则返回包含错误信息的ResponseEntity
     */
    private ResponseEntity<Response> check(PromptRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new Response("提示词不能为空"));
        }
        return null;
    }

    /**
     * 检查提示词参数是否有效
     *
     * @param prompt 提示词参数
     * @return 如果有效则返回null，否则返回包含错误信息的SseEmitter
     */
    private SseEmitter checkPrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new IllegalArgumentException("提示词不能为空"));
            return emitter;
        }
        return null;
    }

    /**
     * 处理POST请求，接收提示词并返回模型响应
     *
     * @param request 包含提示词的请求体
     * @return 包含模型响应的ResponseEntity
     */
    @PostMapping("/chat")
    public ResponseEntity<Response> chat(@RequestBody PromptRequest request) {
        // 检查请求体是否有效
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.getResponse(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/aiservicesimplechat")
    public ResponseEntity<Response> aiServiceSimpleChat(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.aiServiceSimpleChat(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/aiservicetemplatechat")
    public ResponseEntity<Response> aiServiceTemplateChat(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.aiServiceTemplateChat(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/aiservicetemplatechatwithsysmsg")
    public ResponseEntity<Response> aiServiceTemplateChatWithSysMsg(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.aiServiceTemplateChatWithSysMsg(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/simulatemultiroundchat")
    public ResponseEntity<Response> simulateMultiRoundChat(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.simulateMultiRoundChat(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/usechatrequest")
    public ResponseEntity<Response> useChatRequest(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.useChatRequest(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    /**
     * 处理POST请求，接收提示词并返回模型响应
     *
     * @param request 包含提示词的请求体
     * @return 包含模型响应的ResponseEntity
     */
    @PostMapping("/useimage")
    public ResponseEntity<Response> useImage(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.useImage(request.getImageUrl(), request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/generateimage")
    public ResponseEntity<Response> generateImage(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.generateImage(request.getPrompt(), request.getImageNum());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/editimage")
    public ResponseEntity<Response> editImage(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.editImage(request.getImageUrls(), request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/lowlevel/addchatmessagetochatmemory")
    public ResponseEntity<Response> addChatMessageToChatMemory(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.lowLevelAddChatMessageToChatMemory(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/highlevel/ram/global")
    public ResponseEntity<Response> highLevelRamGlobal(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 使用基于内存的全局记忆功能
            String response = qwenService.highLevelRamGlobal(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/highlevel/ram/byuserid")
    public ResponseEntity<Response> highLevelRamByUserID(@RequestBody PromptRequest request) {
        System.out.println("request : " + request);
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }
        try {
            // 使用基于内存的用户ID记忆功能
            String response = qwenService.highLevelRamByUserID(request.getUserId(), request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/highlevel/db/global")
    public ResponseEntity<Response> highLevelDbGlobal(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 使用基于数据库的全局记忆功能
            String response = qwenService.highLevelDbGlobal(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    @PostMapping("/highlevel/db/byuserid")
    public ResponseEntity<Response> highLevelDbByUserID(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 使用基于数据库的用户ID记忆功能
            String response = qwenService.highLevelDbByUserID(request.getUserId(), request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }
    /**
     * SSE流式聊天接口（用于网页实时显示）
     *
     * @param prompt 提示词
     * @param userId 用户ID
     * @return SseEmitter实例，用于向客户端发送流式响应
     */
    @GetMapping(value = "/sse-streaming-chat", produces = "text/event-stream")
    public SseEmitter streamingChat(@RequestParam(name = "prompt") String prompt,
                                    @RequestParam(name = "userId") int userId) {
        // 检查提示词是否有效
        SseEmitter checkRlt = checkPrompt(prompt);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService的流式聊天方法
            return qwenService.lowLevelStreamingChat(prompt);
        } catch (Exception e) {
            // 捕获异常并返回错误的SseEmitter
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }

    /**
     * 基于高级API的SSE流式聊天接口（用于网页实时显示）
     *
     * @param prompt 提示词
     * @param userId 用户ID
     * @return SseEmitter实例，用于向客户端发送流式响应
     */
    @GetMapping(value = "/high-level-sse-streaming-chat", produces = "text/event-stream")
    public SseEmitter highLevelStreamingChat(@RequestParam(name = "prompt") String prompt,
                                             @RequestParam(name = "userId") int userId) {
        // 检查提示词是否有效
        SseEmitter checkRlt = checkPrompt(prompt);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService的高级API流式聊天方法
            return qwenService.highLevelStreamingChat(prompt);
        } catch (Exception e) {
            // 捕获异常并返回错误的SseEmitter
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }



}