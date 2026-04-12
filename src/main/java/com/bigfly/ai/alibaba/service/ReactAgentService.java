package com.bigfly.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.bigfly.ai.alibaba.hook.SensitiveWordHook;
import com.bigfly.ai.alibaba.tools.BaseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Spring AI Alibaba ReactAgent 的 ReAct 服务
 * 使用官方 Agent Framework 内置的完整 ReAct 循环实现：
 * START → AgentLlmNode(AI思考+决策) → AgentToolNode(执行工具) → (循环) → END
 *
 * 对比手写版 ReActService：
 * - 手写版: 自己维护 for 循环 + 字符串拼接轨迹 + 启发式判断是否完成
 * - ReactAgent版: 框架内置状态图路由，自动管理 messages 状态和 Observation 回传
 */
@Slf4j
@Service
public class ReactAgentService {

    private final ReactAgent reactAgent;

    public ReactAgentService(ChatModel chatModel, List<BaseTools> baseToolsList) {
        // 将 BaseTools 转换为 ToolCallback 数组
        List<ToolCallback> allToolCallbacks = new ArrayList<>();
        
        for (BaseTools tool : baseToolsList) {
            if (tool == null || tool.getToolInstance() == null) {
                log.warn("跳过无效工具: {}", tool != null ? tool.getClass().getName() : "null");
                continue;
            }
            
            try {
                Object instance = tool.getToolInstance();
                // 使用 MethodToolCallbackProvider 自动扫描 @Tool 注解方法
                MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                        .toolObjects(instance)
                        .build();
                
                ToolCallback[] callbacks = provider.getToolCallbacks();
                if (callbacks != null && callbacks.length > 0) {
                    for (ToolCallback callback : callbacks) {
                        log.debug("成功加载工具: {}, 工具名: {}", 
                                tool.getClass().getSimpleName(),
                                callback.getToolDefinition().name());
                    }
                    allToolCallbacks.addAll(List.of(callbacks));
                } else {
                    log.warn("工具 {} 没有生成任何 ToolCallback", tool.getClass().getName());
                }
            } catch (Exception e) {
                log.error("创建工具回调失败: {}", tool.getClass().getName(), e);
            }
        }

        ToolCallback[] toolCallbacks = allToolCallbacks.toArray(new ToolCallback[0]);
        log.info("初始化 ReactAgentService，已加载 {} 个工具", toolCallbacks.length);
        // 创建消息压缩 Hook适用场景：
        //超出上下文窗口的长期对话；
        //具有大量历史记录的多轮对话；
        //需要保留完整对话上下文的应用程序。
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(4000)
                .messagesToKeep(20)
                .build();

        // 创建敏感词校验 Hook（传入 ChatModel 做语义审核）
        SensitiveWordHook sensitiveWordHook = new SensitiveWordHook(chatModel);

        // 构建官方 ReactAgent，注册 Hook
        this.reactAgent = ReactAgent.builder()
                .name("react-agent")
                .model(chatModel)
                .tools(toolCallbacks)
                .hooks(List.of(sensitiveWordHook,summarizationHook))   // 通过 Hook 机制实现敏感词校验
                .instruction("""
                        你是一个智能助手。请遵循以下原则：

                        1. 当用户问题需要实时数据（天气、时间、计算等）时，主动调用对应工具获取真实数据
                        2. 根据工具返回的真实结果回答问题，不要编造数据
                        3. 如果已有足够信息直接回答即可
                        """)
                .enableLogging(true)
                .build();
        
        log.info("ReactAgent 初始化完成，已注册 Hook: [SensitiveWordHook, SummarizationHook]");
    }

    /**
     * 执行 ReactAgent 对话
     *
     * @param question 用户问题
     * @return 最终答案
     */
    public String react(String question) {
        log.info("开始 ReactAgent 调用，问题: {}", question);
        try {
            AssistantMessage response = reactAgent.call(question);
            String answer = response.getText();
            log.info("ReactAgent 返回答案: {}", answer);
            return answer;
        } catch (Exception e) {
            log.error("ReactAgent 调用失败", e);
            return "处理请求时出错: " + e.getMessage();
        }
    }
}
