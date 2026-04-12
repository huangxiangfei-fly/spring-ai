package com.bigfly.ai.alibaba.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务
 * 负责文档的加载、分割、向量化和存储
 */
@Slf4j
@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 从文本内容添加文档到向量库
     *
     * @param text      文本内容
     * @param title     文档标题
     * @param source    来源标识
     * @return 文档ID列表
     */
    public List<String> addTextDocument(String text, String title, String source) {
        log.info("开始处理文本文档: {}, 长度: {}", title, text.length());

        // 创建文档对象
        Document document = new Document(
                text,
                java.util.Map.of(
                        "title", title,
                        "source", source,
                        "type", "text"
                )
        );

        // 分割文档为小块
        List<Document> chunks = splitDocument(List.of(document));
        log.info("文档分割完成，共 {} 个片段", chunks.size());

        // 向量化并存储
        vectorStore.accept(chunks);
        log.info("文档已向量化并存储到向量库");

        return chunks.stream().map(Document::getId).toList();
    }

    /**
     * 从文件添加文档到向量库
     *
     * @param file 上传的文件
     * @return 文档ID列表
     */
    public List<String> addFileDocument(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            log.info("开始处理文件: {}, 大小: {} bytes", filename, file.getSize());

            // 读取文件内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 添加到向量库
            return addTextDocument(content, filename, "file:" + filename);
        } catch (IOException e) {
            log.error("文件处理失败", e);
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从类路径资源文件添加文档
     *
     * @param resourcePath 资源路径
     * @return 文档ID列表
     */
    public List<String> addResourceDocument(String resourcePath) {
        try {
            log.info("开始加载资源文件: {}", resourcePath);

            // 加载资源
            Resource resource = new ClassPathResource(resourcePath);
            TextReader reader = new TextReader(resource);

            // 读取文档
            List<Document> documents = reader.get();
            log.info("读取到 {} 个文档", documents.size());

            // 分割文档
            List<Document> chunks = splitDocument(documents);
            log.info("文档分割完成，共 {} 个片段", chunks.size());

            // 向量化并存储
            vectorStore.accept(chunks);
            log.info("文档已向量化并存储到向量库");

            return chunks.stream().map(Document::getId).toList();
        } catch (Exception e) {
            log.error("资源文件处理失败: {}", resourcePath, e);
            throw new RuntimeException("资源文件处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量添加文档
     *
     * @param documents 文档列表
     * @return 文档ID列表
     */
    public List<String> addDocuments(List<Document> documents) {
        log.info("开始批量处理 {} 个文档", documents.size());

        // 分割文档
        List<Document> chunks = splitDocument(documents);
        log.info("文档分割完成，共 {} 个片段", chunks.size());

        // 向量化并存储
        vectorStore.accept(chunks);
        log.info("文档已向量化并存储到向量库");

        return chunks.stream().map(Document::getId).toList();
    }

    /**
     * 删除文档
     *
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        log.info("删除文档: {}", documentIds);
        // SimpleVectorStore 不支持删除操作
        log.warn("当前使用的 SimpleVectorStore 不支持删除操作");
    }

    /**
     * 清空向量库
     */
    public void clearVectorStore() {
        log.info("清空向量库");
        // SimpleVectorStore 不支持清空操作
        log.warn("当前使用的 SimpleVectorStore 不支持清空操作");
    }

    /**
     * 分割文档
     * 使用 TokenTextSplitter 按 token 数量分割文档
     *
     * @param documents 原始文档列表
     * @return 分割后的文档片段列表
     */
    private List<Document> splitDocument(List<Document> documents) {
        // 创建文本分割器
        // 参数说明：
        // - chunkSize: 每个片段的最大 token 数
        // - minChunkSizeChars: 最小字符数
        // - minChunkLengthToEmbed: 最小嵌入长度
        // - maxNumChunks: 最大片段数
        // - keepSeparator: 是否保留分隔符
        TokenTextSplitter splitter = new TokenTextSplitter(
                500,    // 每个片段 500 tokens
                100,    // 最小 100 字符
                50,     // 最小 50 长度才嵌入
                10000,  // 最多 10000 个片段
                true    // 保留分隔符
        );

        return splitter.apply(documents);
    }

    /**
     * 获取向量库统计信息
     *
     * @return 统计信息
     */
    public String getVectorStoreStats() {
        return "当前使用 SimpleVectorStore（内存存储），重启后数据会丢失";
    }
}
