package com.eazybytes.springai.rag;


import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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
        // Load the HR Policies PDF into the Vector Store
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPoliciesRes);

        List<Document> documentList = tikaDocumentReader.get();

        vectorStore.add(documentList);

    }



}
