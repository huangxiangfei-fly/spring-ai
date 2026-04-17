package com.bigfly.ai.alibaba.service;

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 检索服务
 * 负责向量检索
 */
@Slf4j
@Service
public class RetrievalService {

    private final VectorStore vectorStore;
    private final DashScopeRerankModel rerankModel;

    // 默认检索参数
    private static final int DEFAULT_TOP_K = 10;  // 初始检索数量（多一些候选）
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5;
    private static final int RERANK_TOP_N = 5;     // 重排序后返回数量

    public RetrievalService(VectorStore vectorStore, 
                           @Qualifier("dashscopeRerankModel") DashScopeRerankModel rerankModel) {
        this.vectorStore = vectorStore;
        this.rerankModel = rerankModel;
    }

    /**
     * 简单检索（不带重排序）
     *
     * @param query 查询文本
     * @return 相关文档列表
     */
    public List<Document> simpleSearch(String query) {
        return simpleSearch(query, DEFAULT_TOP_K);
    }

    /**
     * 简单检索（自定义返回数量）
     *
     * @param query 查询文本
     * @param topK  返回的文档数量
     * @return 相关文档列表
     */
    public List<Document> simpleSearch(String query, int topK) {
        log.info("执行简单检索，查询: {}, topK: {}", query, topK);

        // 构建搜索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .build();

        // 执行检索
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("检索完成，返回 {} 个结果", results.size());

        return results;
    }

    /**
     * 带重排序的检索（推荐使用）
     * 先进行向量检索，再使用重排序模型对结果进行重排序
     *
     * @param query 查询文本
     * @return 重排序后的相关文档列表
     */
    public List<RerankedDocument> searchWithRerank(String query) {
        return searchWithRerank(query, DEFAULT_TOP_K, RERANK_TOP_N);
    }

    /**
     * 带重排序的检索（自定义参数）
     *
     * @param query       查询文本
     * @param initialTopK 初始检索数量（应该大于最终返回数量）
     * @param finalTopN   重排序后返回的数量
     * @return 重排序后的相关文档列表
     */
    public List<RerankedDocument> searchWithRerank(String query, int initialTopK, int finalTopN) {
        log.info("执行带重排序的检索，查询: {}, initialTopK: {}, finalTopN: {}", 
                query, initialTopK, finalTopN);

        // 第一步：向量检索（召回更多候选）
        List<Document> initialResults = simpleSearch(query, initialTopK);
        
        if (initialResults.isEmpty()) {
            log.warn("未找到相关文档");
            return List.of();
        }

        log.info("向量检索完成，获得 {} 个候选文档，开始重排序...", initialResults.size());

        // 第二步：重排序
        List<RerankedDocument> rerankedResults = rerankDocuments(query, initialResults, finalTopN);
        
        log.info("重排序完成，返回 {} 个结果", rerankedResults.size());

        return rerankedResults;
    }

    /**
     * 对文档进行重排序
     * TODO: 待完善 - 需要根据 Spring AI Alibaba 的实际 API 调整
     *
     * @param query     查询文本
     * @param documents 待重排序的文档列表
     * @param topN      返回的重排序后文档数量
     * @return 重排序后的文档列表（包含相关性分数）
     */
    private List<RerankedDocument> rerankDocuments(String query, List<Document> documents, int topN) {
        try {
            // 调用重排序模型
            var rerankRequest = new RerankRequest(query, documents);
            var rerankResponse = rerankModel.call(rerankRequest);

            // 处理重排序结果
            if (rerankResponse != null && rerankResponse.getResults() != null && !rerankResponse.getResults().isEmpty()) {
                log.info("重排序成功，返回 {} 个结果", rerankResponse.getResults().size());
                
                // TODO: 需要根据实际的 DocumentWithScore API 解析结果
                // 目前先返回原始结果
                return documents.stream()
                        .limit(topN)
                        .map(doc -> new RerankedDocument(doc, 0.0, 0))
                        .collect(Collectors.toList());
            } else {
                log.warn("重排序模型返回空结果，使用原始检索结果");
            }
        } catch (Exception e) {
            log.error("重排序失败，返回原始检索结果", e);
        }
        
        // 如果重排序失败，返回原始结果
        return documents.stream()
                .limit(topN)
                .map(doc -> new RerankedDocument(doc, 0.0, 0))
                .collect(Collectors.toList());
    }

    /**
     * 带过滤条件的检索
     *
     * @param query       查询文本
     * @param filterKey   过滤键
     * @param filterValue 过滤值
     * @return 相关文档列表
     */
    public List<Document> searchWithFilter(String query, String filterKey, String filterValue) {
        log.info("执行带过滤的检索，查询: {}, 过滤条件: {}={}", query, filterKey, filterValue);

        // 构建带过滤条件的搜索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(DEFAULT_TOP_K)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .filterExpression(filterKey + " == '" + filterValue + "'")
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("过滤检索完成，返回 {} 个结果", results.size());

        return results;
    }

    /**
     * 检索文档对象（用于兼容重排序接口）
     * 包含原始文档、相关性分数和排名
     */
    @Data
    public static class RerankedDocument {
        private final Document document;
        private final double relevanceScore;
        private final int rank;

        public RerankedDocument(Document document, double relevanceScore, int rank) {
            this.document = document;
            this.relevanceScore = relevanceScore;
            this.rank = rank;
        }

        /**
         * 获取文档内容
         */
        public String getContent() {
            return document.getText();
        }

        /**
         * 获取文档元数据
         */
        public Object getMetadata(String key) {
            return document.getMetadata().get(key);
        }
    }
}
