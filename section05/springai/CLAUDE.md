# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvn clean install

# Run application
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test
mvn test -Dtest=SpringAiApplicationTests

# Package JAR
mvn clean package
```

## Required Environment Variables

- `OPENAI_API_KEY` — OpenAI API key (used for all LLM calls)
- `TAVILY_SEARCH_API_KEY` — Tavily web search API key (required for `/api/rag/web-search/chat`)

## Infrastructure

Qdrant vector database is required and runs via Docker Compose (`compose.yml`). Spring Boot auto-starts it via `spring-boot-docker-compose`. For tests, Docker Compose is **not** skipped (`spring.docker.compose.skip.in-tests=false`).

Chat memory is persisted via JDBC to an H2 file database at `~/chatmemory-ai-1.1.4-db`. The H2 console is enabled at `/h2-console`.

## Architecture

### Tech Stack
- Java 21, Spring Boot 3.5.13, Spring AI 1.1.4
- OpenAI ChatGPT (model: `gpt-5.4-nano-2026-03-17`)
- Qdrant vector store (collection: `eazybytes`)
- Tavily Search API for web-search RAG
- H2 (JDBC-backed chat memory)
- Apache Tika (PDF document ingestion)

### ChatClient Beans (three configurations)

All ChatClient instances are configured in `config/` and wired into controllers via `@Qualifier`.

| Bean | Config class | Advisors |
|---|---|---|
| `chatClient` | `ChatClientConfig` | SimpleLoggerAdvisor, TokenUsageAuditAdvisor |
| `chatMemoryChatClient` | `ChatMemoryChatClientConfig` | SimpleLoggerAdvisor, MessageChatMemoryAdvisor (15-msg window), TokenUsageAuditAdvisor, RetrievalAugmentationAdvisor (Qdrant + PII masking) |
| `webSearchRAGChatClient` | `WebSearchRAGChatClientConfig` | Advisor-based Tavily web search RAG |

### RAG Pipeline (`rag/` package)

- **`HRPolicyLoader`** — `@PostConstruct` bean that reads `Eazybytes_HR_Policies.pdf` via Tika, splits into 200-token chunks, and ingests into Qdrant at startup.
- **`PIIMaskingDocumentPostProcessor`** — Post-processes retrieved documents before they reach the LLM; replaces emails → `[EMAIL REDACTED]`, phones → `[PHONE REDACTED]`, SSNs → `[SSN REDACTED]`. Uses a pseudo-builder: `builder()` returns the instance directly (no intermediate builder object) for visual consistency with `VectorStoreDocumentRetriever.builder()`.
- **`WebSearchDocumentRetriever`** — Calls Tavily API, translates queries to English, maps results to Spring AI `Document` objects.

The `chatMemoryChatClient` RAG is configured with top-3 retrieval at 0.5 similarity threshold and uses a `TranslationQueryTransformer` to normalize non-English queries before vector search.

### Custom Advisor (`advisors/`)

**`TokenUsageAuditAdvisor`** — `CallAroundAdvisor` that intercepts every response and logs prompt/completion/total token counts from `ChatResponse` metadata.

### REST API Surface

| Endpoint | Controller | Feature |
|---|---|---|
| `GET /api/chat` | `ChatController` | Basic chat (IT helpdesk persona) |
| `GET /api/chat-memory` | `ChatMemoryController` | Conversational memory (username = conversation ID) |
| `GET /api/stream` | `StreamController` | Streaming (Flux) response |
| `GET /api/email` | `PromptTemplateController` | Email drafting via `.st` prompt templates |
| `GET /api/prompt-stuffing` | `PromptStuffingController` | Injected context in prompt |
| `GET /api/rag/random/chat` | `RAGController` | RAG with question-answer advisor |
| `GET /api/rag/document/chat` | `RAGController` | RAG over HR policies (Qdrant) |
| `GET /api/rag/web-search/chat` | `RAGController` | RAG via Tavily web search |
| `GET /api/chat-bean` | `StructuredOutPutController` | Structured output → `CountryCities` bean |
| `GET /api/chat-list` | `StructuredOutPutController` | Structured output → List |
| `GET /api/chat-map` | `StructuredOutPutController` | Structured output → Map |
| `GET /api/chat-bean-list` | `StructuredOutPutController` | Structured output → List of beans |

Prompt templates (`.st` files) live under `src/main/resources/promptTemplates/`.