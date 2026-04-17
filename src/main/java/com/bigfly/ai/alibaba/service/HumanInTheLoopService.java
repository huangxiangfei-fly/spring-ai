package com.bigfly.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.bigfly.ai.alibaba.tools.PoemTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HumanInTheLoopService {

    private final ReactAgent agent;

    public HumanInTheLoopService(ChatModel chatModel, PoemTools poemTools) {
        // 1. 配置检查点
        MemorySaver memorySaver = new MemorySaver();
        
        // 2. 创建人工介入Hook - 监听 poem 工具调用
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("createPoem", ToolConfig.builder()
                        .description("请确认诗歌创作操作")
                        .build())
                .build();
        

        
        // 3. 注册 poem 工具
        var toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(poemTools.getToolInstance())
                .build()
                .getToolCallbacks();
        
        // 4. 创建Agent
        this.agent = ReactAgent.builder()
                .name("poet_agent")
                .model(chatModel)
                .tools(toolCallbacks)
                .hooks(List.of(humanInTheLoopHook))
                .saver(memorySaver)
                .build();
    }

    public String humanLoop(String input)  {
        try {
            String threadId = "user-session-001";
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();

            // 4. 第一次调用 - 触发中断
            System.out.println("=== 第一次调用：期望中断 ===");
            Optional<NodeOutput> result = agent.invokeAndGetOutput(
                    input,
                    config
            );

            // 5. 检查中断并处理
            if (result.isPresent() && result.get() instanceof InterruptionMetadata) {
                InterruptionMetadata interruptionMetadata = (InterruptionMetadata) result.get();

                System.out.println("检测到中断，需要人工审批");
                List<InterruptionMetadata.ToolFeedback> toolFeedbacks =
                        interruptionMetadata.toolFeedbacks();

                for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                    System.out.println("工具: " + feedback.getName());
                    System.out.println("参数: " + feedback.getArguments());
                    System.out.println("描述: " + feedback.getDescription());
                }

                // 6. 模拟人工决策（这里选择拒绝）
                InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                        .nodeId(interruptionMetadata.node())
                        .state(interruptionMetadata.state());

                toolFeedbacks.forEach(toolFeedback -> {
                    InterruptionMetadata.ToolFeedback rejectedFeedback =
                            InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                    .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                    .build();
                    feedbackBuilder.addToolFeedback(rejectedFeedback);
                });
            } else {
                System.out.println("未检测到中断，直接返回结果");
                if (result.isPresent()) {
                    System.out.println("结果: " + result.get());
                    return result.get().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
