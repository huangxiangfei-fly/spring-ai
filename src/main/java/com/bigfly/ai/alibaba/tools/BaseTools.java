package com.bigfly.ai.alibaba.tools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

/**
 * 工具抽象基类
 * 所有工具类继承此类，自动注册为 ToolCallbackProvider
 */
public abstract class BaseTools implements ToolCallbackProvider {

    /**
     * 获取当前工具类的实例
     * 子类必须实现此方法返回自身
     */
    protected abstract Object getToolInstance();

    @Override
    public org.springframework.ai.tool.ToolCallback[] getToolCallbacks() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(getToolInstance())
                .build()
                .getToolCallbacks();
    }
}
