package com.bigfly.ai.alibaba.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 服务
 * 整合检索和生成，实现完整的 RAG 流程
 */
@Slf4j
@Service
public class RagService {

    private final ChatClient chatClient;
    private final RetrievalService retrievalService;
    private final DocumentService documentService;

    // RAG 系统提示词模板
    private static final String RAG_SYSTEM_PROMPT = """
            你是一个智能问答助手，基于提供的上下文信息回答问题。
            
            请遵循以下规则：
            1. 严格基于提供的上下文信息回答问题
            2. 如果上下文中没有相关信息，请明确说明"根据现有资料无法回答该问题"
            3. 回答要准确、简洁、有条理
            4. 引用上下文中的关键信息时，可以适当标注来源
            5. 保持专业和友好的语气
            
            上下文信息：
            {context}
            
            用户问题：{question}
            
            请基于以上上下文信息回答问题：
            """;

    public RagService(ChatModel chatModel, 
                     RetrievalService retrievalService,
                     DocumentService documentService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.retrievalService = retrievalService;
        this.documentService = documentService;
    }

    /**
     * 执行 RAG 查询（带重排序）
     *
     * @param query 用户查询
     * @return RAG 响应结果
     */
    public RagResponse ragQuery(String query) {
        log.info("开始执行 RAG 查询: {}", query);
        
        long startTime = System.currentTimeMillis();

        // 第一步：检索相关文档（带重排序）
        List<RetrievalService.RerankedDocument> rerankedDocs = 
                retrievalService.searchWithRerank(query);

        if (rerankedDocs.isEmpty()) {
            log.warn("未找到相关文档，返回默认回复");
            return new RagResponse(
                    query,
                    "抱歉，我没有找到与您的问题相关的信息。",
                    List.of(),
                    System.currentTimeMillis() - startTime
            );
        }

        // 第二步：构建上下文
        String context = buildContext(rerankedDocs);
        log.debug("构建的上下文长度: {} 字符", context.length());

        // 第三步：生成回答
        String answer = generateAnswer(query, context);
        log.info("RAG 查询完成，耗时: {} ms", System.currentTimeMillis() - startTime);

        // 第四步：构建响应
        List<DocumentInfo> docInfos = rerankedDocs.stream()
                .map(doc -> new DocumentInfo(
                        doc.getContent(),
                        doc.getRelevanceScore(),
                        doc.getRank(),
                        (String) doc.getDocument().getMetadata().get("title"),
                        (String) doc.getDocument().getMetadata().get("source")
                ))
                .collect(Collectors.toList());

        return new RagResponse(
                query,
                answer,
                docInfos,
                System.currentTimeMillis() - startTime
        );
    }

    /**
     * 执行 RAG 查询（简单检索，不带重排序）
     *
     * @param query 用户查询
     * @return RAG 响应结果
     */
    public RagResponse ragQuerySimple(String query) {
        log.info("开始执行简单 RAG 查询: {}", query);
        
        long startTime = System.currentTimeMillis();

        // 第一步：简单检索
        List<Document> documents = retrievalService.simpleSearch(query);

        if (documents.isEmpty()) {
            log.warn("未找到相关文档，返回默认回复");
            return new RagResponse(
                    query,
                    "抱歉，我没有找到与您的问题相关的信息。",
                    List.of(),
                    System.currentTimeMillis() - startTime
            );
        }

        // 第二步：构建上下文
        String context = buildContextFromDocuments(documents);

        // 第三步：生成回答
        String answer = generateAnswer(query, context);
        log.info("简单 RAG 查询完成，耗时: {} ms", System.currentTimeMillis() - startTime);

        // 第四步：构建响应
        List<DocumentInfo> docInfos = documents.stream()
                .map(doc -> new DocumentInfo(
                        doc.getText(),
                        0.0,
                        0,
                        (String) doc.getMetadata().get("title"),
                        (String) doc.getMetadata().get("source")
                ))
                .collect(Collectors.toList());

        return new RagResponse(
                query,
                answer,
                docInfos,
                System.currentTimeMillis() - startTime
        );
    }

    /**
     * 流式 RAG 查询
     *
     * @param query 用户查询
     * @return 流式响应内容
     */
    public String ragQueryStream(String query) {
        log.info("开始执行流式 RAG 查询: {}", query);

        // 检索相关文档（带重排序）
        List<RetrievalService.RerankedDocument> rerankedDocs = 
                retrievalService.searchWithRerank(query);

        if (rerankedDocs.isEmpty()) {
            return "抱歉，我没有找到与您的问题相关的信息。";
        }

        // 构建上下文
        String context = buildContext(rerankedDocs);

        // 流式生成回答
        return chatClient.prompt()
                .system(RAG_SYSTEM_PROMPT.replace("{context}", context).replace("{question}", query))
                .call()
                .content();
    }

    /**
     * 从重排序文档构建上下文
     */
    private String buildContext(List<RetrievalService.RerankedDocument> rerankedDocs) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < rerankedDocs.size(); i++) {
            RetrievalService.RerankedDocument rerankedDoc = rerankedDocs.get(i);
            Document doc = rerankedDoc.getDocument();
            
            context.append("【文档 ").append(i + 1).append("】\n");
            context.append("相关性分数: ").append(String.format("%.4f", rerankedDoc.getRelevanceScore())).append("\n");
            
            // 添加元数据
            String title = (String) doc.getMetadata().get("title");
            String source = (String) doc.getMetadata().get("source");
            if (title != null && !title.isEmpty()) {
                context.append("标题: ").append(title).append("\n");
            }
            if (source != null && !source.isEmpty()) {
                context.append("来源: ").append(source).append("\n");
            }
            
            context.append("内容: ").append(doc.getText()).append("\n\n");
        }
        
        return context.toString();
    }

    /**
     * 从普通文档构建上下文
     */
    private String buildContextFromDocuments(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            
            context.append("【文档 ").append(i + 1).append("】\n");
            
            // 添加元数据
            String title = (String) doc.getMetadata().get("title");
            String source = (String) doc.getMetadata().get("source");
            if (title != null && !title.isEmpty()) {
                context.append("标题: ").append(title).append("\n");
            }
            if (source != null && !source.isEmpty()) {
                context.append("来源: ").append(source).append("\n");
            }
            
            context.append("内容: ").append(doc.getText()).append("\n\n");
        }
        
        return context.toString();
    }

    /**
     * 生成回答
     */
    private String generateAnswer(String question, String context) {
        try {
            return chatClient.prompt()
                    .system(RAG_SYSTEM_PROMPT.replace("{context}", context).replace("{question}", question))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("生成回答失败", e);
            return "抱歉，生成回答时出现错误，请稍后重试。";
        }
    }

    /**
     * RAG 响应对象
     */
    @Data
    public static class RagResponse {
        private final String query;
        private final String answer;
        private final List<DocumentInfo> relatedDocuments;
        private final long processingTimeMs;

        public RagResponse(String query, String answer, List<DocumentInfo> relatedDocuments, long processingTimeMs) {
            this.query = query;
            this.answer = answer;
            this.relatedDocuments = relatedDocuments;
            this.processingTimeMs = processingTimeMs;
        }
    }

    /**
     * 文档信息对象
     */
    @Data
    public static class DocumentInfo {
        private final String content;
        private final double relevanceScore;
        private final int rank;
        private final String title;
        private final String source;

        public DocumentInfo(String content, double relevanceScore, int rank, String title, String source) {
            this.content = content;
            this.relevanceScore = relevanceScore;
            this.rank = rank;
            this.title = title;
            this.source = source;
        }
    }
}
