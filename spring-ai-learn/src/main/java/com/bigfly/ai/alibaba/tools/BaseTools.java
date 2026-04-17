package com.bigfly.ai.alibaba.tools;

/**
 * 工具抽象基类
 * 所有工具类继承此类，实现 getToolInstance() 返回自身
 */
public abstract class BaseTools {

    /**
     * 获取当前工具类的实例
     * 子类必须实现此方法返回 this
     */
    public abstract Object getToolInstance();
}
