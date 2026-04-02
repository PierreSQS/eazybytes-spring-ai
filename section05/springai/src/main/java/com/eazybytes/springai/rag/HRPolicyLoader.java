package com.eazybytes.springai.rag;


import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

// New to Sec5_Chap58
// uses the HRPolicyLoader and the Eazybytes_HR_Policies.pdf file
@Component
public class HRPolicyLoader {

    private final VectorStore vectorStore;

    @Value("classpath:Eazybytes_HR_Policies.pdf")
    public Resource hrPoliciesRes;

    public HRPolicyLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadHRPoliciesFromPDF() {
        // Create a Tika-based reader that will extract text from the injected PDF resource:
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPoliciesRes);

        // Read the extracted documents:
        List<Document> documentList = tikaDocumentReader.get();

        // Builds a token-based splitter
        // with a fixed chunk size and an upper limit on chunks
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(100)
                .withMaxNumChunks(400)
                .build();

        // loads the split documents into the vector store,
        // making them available for retrieval during RAG operations
        vectorStore.add(textSplitter.split(documentList));

    }



}
