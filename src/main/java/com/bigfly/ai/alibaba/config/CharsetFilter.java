package com.bigfly.ai.alibaba.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 字符编码过滤器，确保所有响应使用UTF-8编码
 */
@Component
public class CharsetFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 设置响应字符编码为UTF-8
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 对于SSE流式响应，确保Content-Type包含charset
        String contentType = httpResponse.getContentType();
        if (contentType != null && contentType.contains("text/event-stream")) {
            if (!contentType.contains("charset")) {
                httpResponse.setContentType(contentType + "; charset=" + StandardCharsets.UTF_8.name());
            }
        }
        
        chain.doFilter(request, response);
    }
}