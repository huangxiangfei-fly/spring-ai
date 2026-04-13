package com.bigfly.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            SaRouter
                    .match("/**")    // 拦截所有路径
                    .notMatch(
                        "/static/**",            // 静态资源
                        "/index.html",           // 首页
                        "/chatui/index.html",           // 首页
                        "/index-ali.html",       // 阿里首页
                        "/**/*.css",             // CSS文件
                        "/**/*.js",              // JS文件
                        "/**/*.png",             // PNG图片
                        "/**/*.jpg",             // JPG图片
                        "/**/*.ico"              // ICO图标
                    )
                    .check(r -> StpUtil.checkLogin());  // 登录校验
        })).addPathPatterns("/**").excludePathPatterns("/ali/**");
    }
}
