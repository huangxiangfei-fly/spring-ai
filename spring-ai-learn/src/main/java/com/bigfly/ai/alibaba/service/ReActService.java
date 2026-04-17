package com.bigfly.ai.alibaba.service;

import com.bigfly.ai.alibaba.tools.BaseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct (Reasoning + Acting) 服务
 * 基于 Spring AI 原生工具调用能力，实现思考-行动-观察循环
 *
 * 核心思路：Spring AI 的 .tools() 会自动处理多轮工具调用（内部已实现类 ReAct 循环）
 * 我们在此基础上包装迭代控制和轨迹记录
 */
@Slf4j
@Service
public class ReActService {

    private final ChatClient chatClient;
    private final List<BaseTools> baseToolsList;
    private final Object[] toolObjects;

    // 最大迭代次数
    private static final int MAX_ITERATIONS = 10;

    public ReActService(ChatModel chatModel, List<BaseTools> baseToolsList) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.baseToolsList = baseToolsList;
        this.toolObjects = baseToolsList.stream()
                .map(BaseTools::getToolInstance)
                .toArray();
        log.info("初始化 ReActService，已加载 {} 个工具", toolObjects.length);
    }

    /**
     * 执行 ReAct 循环
     */
    public String react(String question) {
        log.info("========== 开始 ReAct 循环 ==========");
        log.info("问题: {}", question);

        List<String> trajectory = new ArrayList<>();
        String response = null;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            log.info("--- 第 {} 轮 ---", i + 1);

            // 每一轮都把完整历史传给 AI，让它基于已有观察继续推理
            response = think(question, trajectory);
            log.info("本轮响应:\n{}", response);

            trajectory.add(formatStep(i + 1, response));

            // 判断是否已经完成所有需要的工具调用并给出了最终答案
            if (isComplete(response)) {
                log.info("任务完成");
                break;
            }
        }

        log.info("========== ReAct 循环结束 ==========");
        return response != null ? response : "抱歉，无法回答该问题。";
    }

    /**
     * 调用 AI 进行思考和行动
     * 关键：通过 system prompt 引导 AI 主动使用工具，而不是用格式化文本模拟工具调用
     */
    private String think(String question, List<String> trajectory) {
        StringBuilder context = new StringBuilder();

        // 构建用户消息
        context.append(question).append("\n\n");

        if (!trajectory.isEmpty()) {
            context.append("【之前的执行过程】\n");
            for (String step : trajectory) {
                context.append(step).append("\n");
            }
            context.append("\n请根据以上信息，如果还需要更多数据就继续调用工具，否则直接给出最终答案。\n");
        }

        return chatClient.prompt()
                .system(buildSystemPrompt())
                .user(context.toString())
                .tools(toolObjects)
                .call()
                .content();
    }

    /**
     * 构建 system prompt — 核心是让 AI 积极调用工具
     */
    private String buildSystemPrompt() {
        return """
                你是一个智能助手。当用户提问时：
                
                1. 如果问题需要实时数据（天气、时间、计算等），你**必须主动调用对应工具**获取真实数据
                2. 调用工具后，根据返回的真实结果来回答
                3. 不要编造数据！没有工具结果就不要给出具体数值
                4. 如果已经有足够的信息，直接回答即可
                
                你可以使用以下工具获取信息。
                """;
    }

    /**
     * 判断是否已完成（不再需要调用新工具）
     */
    private boolean isComplete(String content) {
        if (content == null) return true;
        // 简单启发式：如果响应看起来像最终答案（不含"需要""查询""调用"等词）
        // 实际上 Spring AI 内部会自动处理多轮工具调用，这里主要做兜底
        return !content.contains("我需要") &&
               !content.contains("让我") &&
               !content.contains("我来查") &&
               !content.contains("让我先") &&
               content.length() > 20;
    }

    private String formatStep(int round, String content) {
        return String.format("[第%d轮] %s", round, content);
    }
}
