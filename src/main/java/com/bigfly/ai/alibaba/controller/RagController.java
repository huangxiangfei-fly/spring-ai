package com.bigfly.ai.alibaba.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bigfly.ai.alibaba.service.DocumentService;
import com.bigfly.ai.alibaba.service.RagService;
import com.bigfly.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 控制器
 * 提供文档管理和 RAG 查询的 HTTP 接口
 */
@Slf4j
@RestController
@RequestMapping("/ali/rag")
@SaIgnore
public class RagController {

    private final RagService ragService;
    private final DocumentService documentService;

    public RagController(RagService ragService, DocumentService documentService) {
        this.ragService = ragService;
        this.documentService = documentService;
    }

    /**
     * RAG 查询（带重排序，推荐）
     * GET /ali/rag/query?query=你的问题
     *
     * @param query 用户查询
     * @return RAG 响应结果
     */
    @GetMapping("/query")
    public Result<RagService.RagResponse> query(@RequestParam String query) {
        log.info("收到 RAG 查询请求: {}", query);
        
        try {
            RagService.RagResponse response = ragService.ragQuery(query);
            return Result.success(response);
        } catch (Exception e) {
            log.error("RAG 查询失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * RAG 查询（简单检索，不带重排序）
     * GET /ali/rag/query/simple?query=你的问题
     *
     * @param query 用户查询
     * @return RAG 响应结果
     */
    @GetMapping("/query/simple")
    public Result<RagService.RagResponse> querySimple(@RequestParam String query) {
        log.info("收到简单 RAG 查询请求: {}", query);
        
        try {
            RagService.RagResponse response = ragService.ragQuerySimple(query);
            return Result.success(response);
        } catch (Exception e) {
            log.error("简单 RAG 查询失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * RAG 流式查询
     * GET /ali/rag/query/stream?query=你的问题
     *
     * @param query 用户查询
     * @return 流式响应
     */
    @GetMapping(value = "/query/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public String queryStream(@RequestParam String query) {
        log.info("收到流式 RAG 查询请求: {}", query);
        
        try {
            return ragService.ragQueryStream(query);
        } catch (Exception e) {
            log.error("流式 RAG 查询失败", e);
            return "查询失败: " + e.getMessage();
        }
    }

    /**
     * 添加文本文档到向量库
     * POST /ali/rag/document/text
     * Body: {"text": "文档内容", "title": "文档标题", "source": "来源"}
     *
     * @param request 文档请求
     * @return 文档ID列表
     */
    @PostMapping("/document/text")
    public Result<List<String>> addTextDocument(@RequestBody TextDocumentRequest request) {
        log.info("收到添加文本文档请求，标题: {}", request.getTitle());
        
        try {
            List<String> documentIds = documentService.addTextDocument(
                    request.getText(),
                    request.getTitle(),
                    request.getSource()
            );
            return Result.success(documentIds);
        } catch (Exception e) {
            log.error("添加文本文档失败", e);
            return Result.error("添加文档失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到向量库
     * POST /ali/rag/document/file
     * Form-Data: file=文件
     *
     * @param file 上传的文件
     * @return 文档ID列表
     */
    @PostMapping("/document/file")
    public Result<List<String>> addFileDocument(@RequestParam("file") MultipartFile file) {
        log.info("收到上传文件请求，文件名: {}, 大小: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        try {
            List<String> documentIds = documentService.addFileDocument(file);
            return Result.success(documentIds);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 从资源文件添加文档
     * POST /ali/rag/document/resource
     * Body: {"resourcePath": "classpath:documents/example.txt"}
     *
     * @param request 资源路径请求
     * @return 文档ID列表
     */
    @PostMapping("/document/resource")
    public Result<List<String>> addResourceDocument(@RequestBody ResourcePathRequest request) {
        log.info("收到添加资源文档请求，路径: {}", request.getResourcePath());
        
        try {
            List<String> documentIds = documentService.addResourceDocument(request.getResourcePath());
            return Result.success(documentIds);
        } catch (Exception e) {
            log.error("添加资源文档失败", e);
            return Result.error("添加文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取向量库统计信息
     * GET /ali/rag/stats
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, String>> getStats() {
        try {
            String stats = documentService.getVectorStoreStats();
            Map<String, String> result = new HashMap<>();
            result.put("stats", stats);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量初始化示例文档
     * GET /ali/rag/init/sample
     *
     * @return 初始化结果
     */
    @GetMapping("/init/sample")
    public Result<String> initSampleDocuments() {
        log.info("开始初始化示例文档");
        
        try {
            // 示例1：添加 Spring AI 相关文档
            String springAiDoc = """
                    Spring AI 是一个用于构建 AI 应用程序的 Spring 框架。
                    
                    主要特性：
                    1. 统一的 API 抽象：支持多种 AI 模型提供商（OpenAI、Azure OpenAI、Anthropic、Ollama 等）
                    2. 同步和流式 API：支持阻塞式和响应式编程模型
                    3. 函数调用：支持将 Java 方法暴露给 AI 模型
                    4. 向量存储：支持多种向量数据库（Redis、Milvus、PGVector 等）
                    5. RAG 支持：内置检索增强生成功能
                    6. 聊天记忆：支持对话历史管理
                    
                    核心组件：
                    - ChatClient：简化的聊天客户端 API
                    - ChatModel：聊天模型抽象
                    - EmbeddingModel：嵌入模型抽象
                    - VectorStore：向量存储抽象
                    - ToolCallback：工具回调机制
                    """;

            documentService.addTextDocument(
                    springAiDoc,
                    "Spring AI 介绍",
                    "sample:spring-ai-intro"
            );

            // 示例2：添加 RAG 相关文档
            String ragDoc = """
                    RAG（Retrieval-Augmented Generation，检索增强生成）是一种结合信息检索和文本生成的技术。
                    
                    RAG 工作流程：
                    1. 文档处理：加载、清洗、分割文档
                    2. 向量化：使用嵌入模型将文本转换为向量
                    3. 存储：将向量存入向量数据库
                    4. 检索：根据查询向量检索相似文档
                    5. 重排序：对检索结果进行相关性排序（可选）
                    6. 生成：将检索到的上下文和用户查询一起发送给 LLM 生成回答
                    
                    RAG 的优势：
                    - 减少幻觉：基于真实文档生成回答
                    - 知识更新：无需重新训练模型即可更新知识
                    - 可追溯性：可以追溯回答的来源
                    - 成本效益：比微调模型更经济
                    
                    应用场景：
                    - 智能客服
                    - 企业知识库问答
                    - 文档检索系统
                    - 研究助手
                    """;

            documentService.addTextDocument(
                    ragDoc,
                    "RAG 技术介绍",
                    "sample:rag-intro"
            );

            // 示例3：添加阿里巴巴通义千问相关文档
            String qwenDoc = """
                    通义千问（Qwen）是阿里巴巴通义实验室独立开发的大型语言模型。
                    
                    主要特点：
                    1. 强大的语言能力：支持多语言交互，包括中文、英文等
                    2. 超长上下文：支持超长的输入上下文窗口
                    3. 深度推理：具备复杂的逻辑推理和数学计算能力
                    4. 全栈代码赋能：支持代码生成、理解和调试
                    5. 自主智能体：可以自主完成多轮搜索和执行复杂任务
                    6. 深度视觉分析：能够解析复杂的图表和科学示意图
                    
                    模型版本：
                    - qwen-turbo：速度快，成本低
                    - qwen-plus：平衡性能和成本
                    - qwen-max：性能最强，适合复杂任务
                    - qwen-long：支持超长文本处理
                    
                    DashScope API：
                    阿里云 DashScope 平台提供了通义千问的 API 服务，
                    支持聊天、嵌入、图像生成等多种功能。
                    """;

            documentService.addTextDocument(
                    qwenDoc,
                    "通义千问介绍",
                    "sample:qwen-intro"
            );

            log.info("示例文档初始化完成");
            return Result.success("示例文档初始化成功，共添加 3 个文档");
            
        } catch (Exception e) {
            log.error("初始化示例文档失败", e);
            return Result.error("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 文本文档请求对象
     */
    @lombok.Data
    public static class TextDocumentRequest {
        private String text;
        private String title;
        private String source;
    }

    /**
     * 资源路径请求对象
     */
    @lombok.Data
    public static class ResourcePathRequest {
        private String resourcePath;
    }
}
