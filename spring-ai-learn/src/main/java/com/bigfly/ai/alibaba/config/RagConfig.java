package com.bigfly.ai.alibaba.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 配置类
 * 配置向量存储、嵌入模型、重排序模型等
 */
@Configuration
public class RagConfig {

    /**
     * 创建向量存储
     * 使用 SimpleVectorStore（内存存储）
     * 注意：重启后数据会丢失，生产环境建议使用 Redis Stack 或 Milvus
     */
    @Bean
    public VectorStore vectorStore(@Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
