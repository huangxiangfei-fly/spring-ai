package com.bigfly.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final ReactAgent agent;

    public SkillService(ChatModel chatModel) {
     //   1. 技能注册表：从 classpath:skills 加载（如 src/main/resources/skills/）
        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();
// 2. Skills Hook：注册 read_skill 工具并注入技能列表到系统提示
        SkillsAgentHook hook = SkillsAgentHook.builder()
                .skillRegistry(registry)
                .build();

        // 3. Shell Hook：提供 Shell 命令执行（工作目录可指定，如当前工程目录）
        ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("user.dir")).build())
                .build();

        this.agent = ReactAgent.builder()
                .name("skills-agent")
                .model(chatModel)
                .hooks(List.of(hook,shellHook))
                .saver(new MemorySaver())
                .enableLogging(true)
                .build();
    }
}
