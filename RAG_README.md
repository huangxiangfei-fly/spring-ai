# Spring AI Alibaba RAG 完整实现

本项目实现了基于 Alibaba AI (通义千问) 的完整 RAG (检索增强生成) 系统。

## 📁 项目结构

```
com.bigfly.ai.alibaba
├── config/
│   ├── ChatModelConfig.java      # 聊天模型配置
│   ├── CharsetFilter.java         # 字符集过滤器
│   └── RagConfig.java             # RAG 配置（向量存储）
├── controller/
│   ├── AlibabaAiChatController.java  # 基础聊天控制器
│   └── RagController.java            # RAG 控制器（核心）
└── service/
    ├── DocumentService.java       # 文档服务（向量化）
    ├── RetrievalService.java      # 检索服务（向量检索）
    └── RagService.java            # RAG 服务（检索+生成）
```

## ✨ 核心功能

### 1. 文档管理 (DocumentService)
- ✅ 文本文档添加
- ✅ 文件上传处理
- ✅ 资源文件加载
- ✅ 自动文档分割 (TokenTextSplitter)
- ✅ 向量化存储 (DashScope Embedding)

### 2. 向量检索 (RetrievalService)
- ✅ 语义相似度检索
- ✅ 可配置检索参数 (topK, 相似度阈值)
- ✅ 过滤条件检索
- ⏳ 重排序功能 (预留接口，待集成 DashScope Rerank)

### 3. RAG 查询 (RagService)
- ✅ 简单检索模式
- ✅ 带重排序的高级模式 (当前返回普通检索结果)
- ✅ 流式响应支持
- ✅ 上下文构建与提示词工程
- ✅ 相关文档溯源

## 🚀 快速开始

### 1. 启动应用

确保已配置 API Key (在 `application.yml` 中):
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:your-api-key}
      chat:
        options:
          model: qwen-plus
```

### 2. 初始化示例文档

```bash
GET http://localhost:8080/ali/rag/init/sample
```

这会添加 3 个示例文档：
- Spring AI 介绍
- RAG 技术介绍
- 通义千问介绍

### 3. 执行 RAG 查询

#### 方式一：带重排序的查询（推荐）
```bash
GET http://localhost:8080/ali/rag/query?query=什么是RAG技术
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "query": "什么是RAG技术",
    "answer": "RAG（Retrieval-Augmented Generation，检索增强生成）是一种...",
    "relatedDocuments": [
      {
        "content": "RAG 工作流程：1. 文档处理...",
        "relevanceScore": 0.0,
        "rank": 0,
        "title": "RAG 技术介绍",
        "source": "sample:rag-intro"
      }
    ],
    "processingTimeMs": 1234
  }
}
```

#### 方式二：简单检索查询
```bash
GET http://localhost:8080/ali/rag/query/simple?query=Spring AI有哪些特性
```

#### 方式三：流式查询
```bash
GET http://localhost:8080/ali/rag/query/stream?query=RAG的工作流程
```

### 4. 添加自定义文档

#### 添加文本文档
```bash
POST http://localhost:8080/ali/rag/document/text
Content-Type: application/json

{
  "text": "Java 是一种广泛使用的面向对象编程语言...",
  "title": "Java 介绍",
  "source": "custom:java"
}
```

#### 上传文件
```bash
POST http://localhost:8080/ali/rag/document/file
Content-Type: multipart/form-data

file: [选择文件]
```

#### 从资源文件添加
```bash
POST http://localhost:8080/ali/rag/document/resource
Content-Type: application/json

{
  "resourcePath": "documents/rag-guide.txt"
}
```

## 📊 技术架构

### 向量化流程
```
原始文档 → 文档加载 → 文本分割 → 向量化 → 向量存储
           (Reader)  (Splitter) (Embedding) (VectorStore)
```

### RAG 查询流程
```
用户查询 → 向量检索 → 构建上下文 → LLM 生成 → 返回答案
           (Retrieval) (Context)   (Generation)
```

### 核心组件

1. **Embedding Model**: DashScope Embedding
   - 将文本转换为向量表示
   - 支持中英文

2. **Vector Store**: SimpleVectorStore (内存存储)
   - 开发环境使用
   - 生产环境建议切换到 Redis/Milvus/PGVector

3. **Text Splitter**: TokenTextSplitter
   - 按 token 数量分割文档
   - 默认每块 500 tokens

4. **Chat Model**: Qwen Plus
   - 基于检索到的上下文生成回答
   - 支持流式输出

## 🔧 配置说明

### 文档分割参数 (DocumentService)
```java
TokenTextSplitter splitter = new TokenTextSplitter(
    500,    // 每个片段的最大 token 数
    100,    // 最小字符数
    50,     // 最小嵌入长度
    10000,  // 最大片段数
    true    // 保留分隔符
);
```

### 检索参数 (RetrievalService)
```java
private static final int DEFAULT_TOP_K = 5;              // 返回文档数量
private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5;  // 相似度阈值
```

### RAG 提示词模板 (RagService)
```java
private static final String RAG_SYSTEM_PROMPT = """
    你是一个智能问答助手，基于提供的上下文信息回答问题。
    
    请遵循以下规则：
    1. 严格基于提供的上下文信息回答问题
    2. 如果上下文中没有相关信息，请明确说明
    3. 回答要准确、简洁、有条理
    ...
    """;
```

## 📝 API 接口列表

| 接口 | 方法 | 说明 |
|------|------|------|
| `/ali/rag/init/sample` | GET | 初始化示例文档 |
| `/ali/rag/query` | GET | RAG 查询（带重排序接口） |
| `/ali/rag/query/simple` | GET | RAG 查询（简单检索） |
| `/ali/rag/query/stream` | GET | RAG 流式查询 |
| `/ali/rag/document/text` | POST | 添加文本文档 |
| `/ali/rag/document/file` | POST | 上传文件 |
| `/ali/rag/document/resource` | POST | 添加资源文档 |
| `/ali/rag/stats` | GET | 获取统计信息 |

## 🧪 测试

使用 `src/main/resources/rag-test.http` 文件进行 API 测试：

1. 在 IntelliJ IDEA 中打开该文件
2. 点击每个请求旁边的运行按钮
3. 查看响应结果

## 🎯 最佳实践

### 1. 文档质量
- 保持文档内容清晰、结构化
- 适当添加标题和分段
- 避免过长的单一文档

### 2. 检索优化
- 根据场景调整 topK (建议 5-10)
- 调整相似度阈值 (建议 0.5-0.7)
- 使用元数据过滤提高精度

### 3. 提示词工程
- 明确定义助手角色
- 设置清晰的回答规则
- 提供足够的上下文信息

### 4. 性能优化
- 生产环境使用持久化向量数据库
- 实现文档缓存机制
- 异步处理文档向量化

## 🔮 未来改进

- [ ] 集成 DashScope Rerank 模型实现真正的重排序
- [ ] 支持更多向量数据库 (Redis, Milvus, PGVector)
- [ ] 添加文档更新和删除功能
- [ ] 实现多轮对话记忆
- [ ] 支持图片、PDF 等更多文档格式
- [ ] 添加评估指标和监控

## 📚 参考资料

- [Spring AI 官方文档](https://spring.io/projects/spring-ai)
- [Alibaba DashScope API](https://help.aliyun.com/zh/dashscope/)
- [RAG 技术介绍](https://arxiv.org/abs/2005.11401)

## 💡 常见问题

**Q: 检索结果为空怎么办？**
A: 检查文档是否已成功向量化，降低相似度阈值，或增加 topK 值。

**Q: 如何提高回答质量？**
A: 优化文档质量，调整检索参数，优化提示词模板，启用重排序功能。

**Q: SimpleVectorStore 适合生产环境吗？**
A: 不适合。SimpleVectorStore 是内存存储，重启后数据丢失。生产环境建议使用 Redis、Milvus 等持久化向量数据库。

## 📄 许可证

本项目仅供学习和参考使用。
