package com.eazybytes.springai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.List;

public class WebSearchDocumentRetriever implements DocumentRetriever {

    private static final Logger logger = LoggerFactory.getLogger(WebSearchDocumentRetriever.class);

    private static final String TAVILY_API_KEY = "TAVILY_API_KEY";
    private static final String TAVILY_BASE_URL = "https://tavily.com/api/v1/search";
    private static final int DEFAULT_RESULT_LIMIT = 10;

    private final int resultLimit;
    private final RestClient restClient;

    public WebSearchDocumentRetriever(RestClient.Builder clientBuilder, int resultLimit) {
        Assert.notNull(clientBuilder, "clientBuilder cannot be null");
        String apiKey = System.getenv(TAVILY_API_KEY);
        Assert.hasText(apiKey, "Environment variable '" + TAVILY_API_KEY + "' must be set");
        this.restClient = clientBuilder
                .baseUrl(TAVILY_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        // Alternative to throwing an IllegalStateException like EazyBytes
        Assert.isTrue(resultLimit > 0, "resultLimit must be greater than 0");
        this.resultLimit = resultLimit;

        logger.info("Created WebSearchDocumentRetriever with resultLimit= {}", resultLimit);
    }

    /**
     * Retrieves relevant documents from an underlying data source based on the given
     * query.
     *
     * @param query The query to use for retrieving documents
     * @return The list of relevant documents
     */
    @NonNull
    @Override
    public List<Document> retrieve(@NonNull Query query) {
        return List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Builder for this class
    public static class Builder {
        private RestClient.Builder restClientBuilder;
        private int resultLimit = DEFAULT_RESULT_LIMIT;

        // prevents external code from calling new Builder() directly
        private Builder() {}

        public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
            this.restClientBuilder = restClientBuilder;
            return this;
        }

        public Builder maxResults(int maxResults) {
            // Alternative to throwing an IllegalStateException like EazyBytes
            Assert.isTrue(resultLimit > 0, "maxResults must be greater than 0");
            this.resultLimit = maxResults;
            return this;
        }

        public WebSearchDocumentRetriever build() {
            return new WebSearchDocumentRetriever(this.restClientBuilder, this.resultLimit);
        }
    }
}
