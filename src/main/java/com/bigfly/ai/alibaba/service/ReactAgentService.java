package com.bigfly.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.bigfly.ai.alibaba.tools.BaseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.stereotype.Service;

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
        // 将 BaseTools 转换为 ToolCallback 数组，过滤掉无效的工具
        ToolCallback[] toolCallbacks = baseToolsList.stream()
                .filter(tool -> {
                    if (tool == null) {
                        log.warn("发现 null 工具实例，已跳过");
                        return false;
                    }
                    Object instance = tool.getToolInstance();
                    if (instance == null) {
                        log.warn("工具 {} 的实例为 null，已跳过", tool.getClass().getName());
                        return false;
                    }
                    return true;
                })
                .map(tool -> {
                    try {
                        Object instance = tool.getToolInstance();
                        log.debug("正在创建工具回调: {}, 实例: {}", tool.getClass().getSimpleName(), instance);
                        return MethodToolCallback.builder()
                                .toolObject(instance)
                                .build();
                    } catch (Exception e) {
                        log.error("创建工具回调失败: {}, 错误: {}", tool.getClass().getName(), e.getMessage());
                        return null;
                    }
                })
                .filter(callback -> callback != null)
                .toArray(ToolCallback[]::new);

        log.info("初始化 ReactAgentService，已加载 {} 个工具", toolCallbacks.length);

        // 构建官方 ReactAgent
        this.reactAgent = ReactAgent.builder()
                .name("react-agent")
                .model(chatModel)
                .tools(toolCallbacks)
                .instruction("""
                        你是一个智能助手。请遵循以下原则：

                        1. 当用户问题需要实时数据（天气、时间、计算等）时，主动调用对应工具获取真实数据
                        2. 根据工具返回的真实结果回答问题，不要编造数据
                        3. 如果已有足够信息直接回答即可
                        """)
                .enableLogging(true)
                .build();
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
