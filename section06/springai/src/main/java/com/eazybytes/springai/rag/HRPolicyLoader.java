package com.eazybytes.springai.rag;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HRPolicyLoader {

    private final VectorStore vectorStore;

    @Value("classpath:Eazybytes_HR_Policies.pdf") private Resource hrPolicyFile;

    public HRPolicyLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadPoliciesPDF() {

        log.info("##### Loading HR policies... #####");

        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPolicyFile);
        List<Document> documents = tikaDocumentReader.get();

        TextSplitter textSplitter =
                TokenTextSplitter.builder()
                        .withChunkSize(100)
                        .withMaxNumChunks(400)
                        .build();

        vectorStore.add(textSplitter.split(documents));

        log.info("##### Loading HR policies complete. #####");
    }


}
