package com.bigfly.ai.alibaba.config;

import com.bigfly.ai.alibaba.tools.BaseTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Tool 配置类 - 使用工厂模式自动注册所有工具
 * 新增工具只需：
 * 1. 创建类继承 BaseTools
 * 2. 添加 @Component 和 @Tool 注解
 * 无需修改此配置
 */
@Configuration
public class ToolConfig {

    /**
     * 自动收集并注册所有继承 BaseTools 的工具类
     * Spring 会自动注入所有 BaseTools 子类实例
     */
    @Bean
    public ToolCallbackProvider toolCallbackProviderFactory(List<BaseTools> baseToolsList) {
        // 将所有 BaseTools 合并为一个 Provider
        return new ToolCallbackProvider() {
            @Override
            public org.springframework.ai.tool.ToolCallback[] getToolCallbacks() {
                return baseToolsList.stream()
                        .flatMap(baseTools -> java.util.Arrays.stream(baseTools.getToolCallbacks()))
                        .toArray(org.springframework.ai.tool.ToolCallback[]::new);
            }
        };
    }
}
