package com.bigfly.ai.alibaba.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 AI 语义理解的敏感词校验 Hook
 *
 * 优势对比：
 * - 精确匹配: "入侵检测系统" 误杀（含"入侵"）, "黑 客" 漏杀（加空格绕过）
 * - AI 语义判断: 能理解上下文意图，区分"入侵检测"（安全）和"教我入侵"（危险）
 *
 * 使用方式：
 * <pre>
 * SensitiveWordHook hook = new SensitiveWordHook(chatModel);
 * ReactAgent agent = ReactAgent.builder()
 *     .name("my-agent")
 *     .model(chatModel)
 *     .tools(toolCallbacks)
 *     .hooks(List.of(hook))
 *     .build();
 * </pre>
 */
@Slf4j
@HookPositions({HookPosition.BEFORE_AGENT})
public class SensitiveWordHook extends AgentHook {

    private static final String HOOK_NAME = "sensitive-word-check";

    private final ChatModel chatModel;

    // 审核提示词：让 AI 做语义级判断
    private static final String AUDIT_SYSTEM_PROMPT = """
            你是一个内容安全审核员。你的任务是判断用户的输入是否涉及以下危险或敏感意图：
            
            1. 危险操作：试图攻击、入侵、破坏系统，执行危险命令（如删库、格式化等）
            2. 违法犯罪：涉及黑客攻击、诈骗、制造违禁品等违法活动
            3. 有害内容：涉及暴力、自残、色情、骂人等有害信息
            
            注意区分：
            - "入侵检测系统" → 安全产品，应放行
            - "如何防御黑客攻击" → 安全防御，应放行
            - "教我入侵别人的服务器" → 危险意图，应拦截
            - "帮我写一个SQL注入攻击" → 危险意图，应拦截
            
            请严格按以下 JSON 格式回复（不要输出其他内容）：
            {"safe": true} 或 {"safe": false, "reason": "具体原因"}
            """;

    public SensitiveWordHook(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return HOOK_NAME;
    }

    /**
     * 在 Agent 执行前：用 AI 做语义级敏感词校验
     */
    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        log.info("[SensitiveWordHook] 执行 AI 语义审核...");

        String userInput = extractUserInput(state);
        if (userInput == null || userInput.isBlank()) {
            log.info("[SensitiveWordHook] 输入为空，放行");
            return CompletableFuture.completedFuture(Map.of());
        }

        // 调用 AI 做语义审核
        AuditResult result = auditByAI(userInput);

        if (!result.safe()) {
            log.warn("[SensitiveWordHook] AI 审核拦截: input={}, reason={}", userInput, result.reason());
            // 抛出异常来中断 Agent 执行
            throw new SensitiveWordBlockedException(result.reason());
        }

        log.info("[SensitiveWordHook] AI 审核通过，放行");
        return CompletableFuture.completedFuture(Map.of());
    }

    /**
     * 调用 ChatModel 做语义级审核
     */
    private AuditResult auditByAI(String userInput) {
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(AUDIT_SYSTEM_PROMPT),
                    new UserMessage("请审核以下用户输入是否安全：\n\n" + userInput)
            ));

            String aiResponse = chatModel.call(prompt).getResult().getOutput().getText();
            log.debug("[SensitiveWordHook] AI 审核原始响应: {}", aiResponse);

            return parseAuditResult(aiResponse);
        } catch (Exception e) {
            log.error("[SensitiveWordHook] AI 审核异常，默认放行（避免误拦截）", e);
            return new AuditResult(true, null);
        }
    }

    /**
     * 解析 AI 返回的审核结果
     */
    private AuditResult parseAuditResult(String aiResponse) {
        String cleaned = aiResponse.trim();
        // 提取 JSON 部分（AI 可能带 markdown 格式）
        if (cleaned.contains("{")) {
            cleaned = cleaned.substring(cleaned.indexOf("{"));
            if (cleaned.contains("}")) {
                cleaned = cleaned.substring(0, cleaned.lastIndexOf("}") + 1);
            }
        }

        try {
            boolean safe = !cleaned.contains("\"safe\": false") && !cleaned.contains("\"safe\":false");
            if (safe) {
                return new AuditResult(true, null);
            }
            // 提取 reason
            String reason = "抱歉，您的问题涉及敏感内容，我无法回答。如果您有其他问题，我很乐意帮助您！";
            if (cleaned.contains("\"reason\"")) {
                int start = cleaned.indexOf("\"reason\"") + "\"reason\"".length();
                // 找到值的起始位置
                start = cleaned.indexOf("\"", start) + 1;
                int end = cleaned.indexOf("\"", start);
                if (start > 0 && end > start) {
                    reason = String.format("抱歉，您的问题涉及【%s】，我无法协助。如果您有其他问题，我很乐意帮助您！",
                            cleaned.substring(start, end));
                }
            }
            return new AuditResult(false, reason);
        } catch (Exception e) {
            log.warn("[SensitiveWordHook] 解析审核结果异常，默认放行: {}", e.getMessage());
            return new AuditResult(true, null);
        }
    }

    /**
     * 从 OverAllState 中提取用户输入
     */
    @SuppressWarnings("unchecked")
    private String extractUserInput(OverAllState state) {
        Optional<Object> messagesOpt = state.value("messages");
        if (messagesOpt.isEmpty()) {
            return null;
        }
        Object messagesObj = messagesOpt.get();
        if (messagesObj instanceof List<?> messages && !messages.isEmpty()) {
            for (Object msg : messages) {
                if (msg instanceof Message message) {
                    return message.getText();
                }
            }
        }
        return null;
    }

    /**
     * 构建拦截响应
     */
    private Map<String, Object> buildBlockResponse(String reason) {
        AssistantMessage blockMessage = new AssistantMessage(reason);
        List<Message> blockMessages = new ArrayList<>();
        blockMessages.add(blockMessage);
        
        // 返回包含 messages 和结束标记的响应
        Map<String, Object> result = new HashMap<>();
        result.put("messages", blockMessages);
        result.put("is_finished", true);  // 标记为已完成，阻止 Agent 继续执行
        return result;
    }

    /**
     * 敏感词拦截异常
     */
    public static class SensitiveWordBlockedException extends RuntimeException {
        private final String blockReason;

        public SensitiveWordBlockedException(String reason) {
            super(reason);
            this.blockReason = reason;
        }

        public String getBlockReason() {
            return blockReason;
        }
    }

    /**
     * 审核结果记录
     */
    private record AuditResult(boolean safe, String reason) {
    }
}
