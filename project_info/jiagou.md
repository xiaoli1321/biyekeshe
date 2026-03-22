# 知识库系统架构设计 (Knowledge Base Architecture)

## 1. 总体设计原则 (Architect's Decision)
- **解耦设计**：知识库模块应与现有的课程、章节模块解耦，通过独立的服务进行管理。
- **混合检索策略 (Hybrid Search)**：结合传统的全文检索（关键词召回）与现代的向量检索（语义召回），通过重排序提升 RAG 效果。
- **可扩展性**：向量数据库、解析策略应具备插件化接口，方便后续增加 Milvus/Elasticsearch 或更复杂的对齐算法。

## 2. 核心组件
- **Document Ingestion Interface**：支持海量文本录入，适配各种非结构化文档。
- **Parsing & Chunking Engine**：自研分块逻辑，确保语境上下文的完整性。
- **Search & Retrieval Core**：
    - **Keyword Engine**：依托 MongoDB Text Index。
    - **Vector Engine**：初期采用嵌入式向量存储（如 Simple-Vector 或扩展 MongoDB）。
- **Reranker**：引入 Cross-Encoder 模型对召回结果进行二次打分。
- **Generation Controller**：结合 Spring WebFlux SSE，对接 LLM 进行流式知识问答。

## 3. 核心流程 (Data Flow)
1. **录入流程**：`Upload(MultipartFile)` -> `DocumentParserService` -> `Chunking` -> `Embedding` -> `Storage(Mongo & VectorDB)`.
2. **检索流程**：`UserQuery` -> `Embedding(Query)` -> `ParallelRetrieval(Vector & Keyword)` -> `Union & Deduplication` -> `Reranking` -> `Prompt Construction` -> `LLM Generation(SSE)`.