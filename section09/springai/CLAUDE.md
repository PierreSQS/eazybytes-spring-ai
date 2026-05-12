# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Context

This is Section 9 of the EazyBytes Spring AI Udemy course — a demo project showcasing enterprise Spring AI patterns with observability. It is not a production application.

- **Spring Boot:** 3.5.14
- **Spring AI:** 1.1.5
- **Java:** 21
- **AI Provider:** OpenAI (GPT-4.1-Mini default, GPT-4 for some endpoints)

## Build & Run Commands

```bash
# Build
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SpringAiApplicationTests

# Start infrastructure (Qdrant, Prometheus, Grafana, Jaeger)
docker compose up -d
```

The application requires an `OPENAI_API_KEY` environment variable. RAG web search also requires `TAVILY_API_KEY`.

## Architecture

### Package Layout (`com.eazybytes.springai`)

| Package | Purpose |
|---------|---------|
| `config/` | ChatClient beans — one config class per feature (chat memory, RAG, tool use, web search) |
| `controller/` | REST endpoints — one controller per Spring AI feature being demonstrated |
| `advisors/` | Custom `TokenUsageAuditAdvisor` — logs token consumption per call |
| `rag/` | Document loaders, PII masking post-processor, web search document retriever |
| `tools/` | `@Tool`-annotated methods for function calling (`HelpDeskTools`, `TimeTools`) |
| `service/` + `repository/` + `entity/` | JPA-backed HelpDesk ticket lifecycle (used by tool-calling demo) |

### Feature → Config → Controller Mapping

Each Spring AI feature has a dedicated `Config` class that produces a `ChatClient` bean, injected into the matching controller:

| Feature | Config Bean | Controller |
|---------|------------|------------|
| Basic chat | `ChatClientConfig` | `ChatController` |
| Streaming | _(reuses basic)_ | `StreamController` |
| Chat memory (JDBC) | `ChatMemoryChatClientConfig` | `ChatMemoryController` |
| RAG (vector store) | `ChatMemoryChatClientConfig` | `RAGController` |
| Web search RAG | `WebSearchRAGChatClientConfig` | `RAGController` |
| Tool calling | `HelpDeskChatClientConfig` | `HelpDeskController` |
| Time tools | `TimeChatClientConfig` | `TimeController` |

### RAG Pipeline

- **Vector store:** Qdrant (port 6334, collection `eazybytes`, cosine similarity ≥ 0.5, top-K 3)
- **Document loading:** `HRPolicyLoader` (Tika), `RandomDataLoader` — both load on startup
- **Post-processing:** `PIIMaskingDocumentPostProcessor` strips PII before indexing
- **Query translation:** Queries are translated to English before retrieval
- **Web search path:** `WebSearchDocumentRetriever` uses Tavily API as an alternative retriever

### Chat Memory

Stored in H2 file database at `~/chatmemory-ai-1.1.4-db`. Schema managed by `spring.jpa.hibernate.ddl-auto: update`. Conversation window: 10 messages (`MessageWindowChatMemory`).

### Observability Stack

All four services are defined in `compose.yml` and must be running for full observability:

| Service | Port | Purpose |
|---------|------|---------|
| Qdrant | 6333/6334 | Vector store |
| Prometheus | 9090 | Scrapes `/actuator/prometheus` |
| Grafana | 3000 | Dashboards |
| Jaeger | 16686 | Distributed traces (OTLP ingest on 4317) |

OpenTelemetry sampling is 100% (`probability: 1.0`). Spring AI chat advisor logs are at DEBUG level.

### Prompt Templates

`.st` (StringTemplate) files in `src/main/resources/promptTemplates/`:
- `userPromptTemplate.st` — email response template
- `systemPromptTemplate.st` — HR assistant system prompt
- `systemPromptRandomDataTemplate.st` — random data system prompt
- `helpDeskSystemPromptTemplate.st` — HelpDesk system prompt