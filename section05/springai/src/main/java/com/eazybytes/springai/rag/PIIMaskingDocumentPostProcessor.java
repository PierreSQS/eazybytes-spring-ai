package com.eazybytes.springai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;


/**
 * Masks sensitive information (e.g., emails, phone numbers) in document content
 * to ensure privacy and compliance. Uses regex patterns to identify and redact PII.
 */
public class PIIMaskingDocumentPostProcessor implements DocumentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(PIIMaskingDocumentPostProcessor.class);

    // Regex pattern for email addresses (e.g., user@example.com)
    private static final String EMAIL_PATTERN =
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}";

    // Regex pattern for phone numbers supporting common formats:
    // 123-456-7890, (123) 456-7890, 123.456.7890, +1 123 456 7890
    private static final String PHONE_PATTERN =
            "\\b(\\+?\\d{1,3}[\\s.\\-]?)?(\\(?\\d{3}\\)?[\\s.\\-]?)\\d{3}[\\s.\\-]?\\d{4}\\b";

    // Regex pattern for US Social Security Numbers (e.g., 123-45-6789)
    private static final String SSN_PATTERN =
            "\\b\\d{3}-\\d{2}-\\d{4}\\b";

    @NonNull
    @Override
    public List<Document> process(@NonNull Query query, @NonNull List<Document> documents) {
        Assert.notEmpty(documents, "Documents list must not be empty");

        log.debug("Masking sensitive information in documents for query: {}", query.text());

        // Stream through each retrieved document and replace any detected PII
        // with clearly labeled redaction placeholders before returning to the caller
        return documents.stream()
                .map(document -> {
                    // Use empty string as fallback if document text is null
                    String maskedText = Objects.requireNonNullElse(document.getText(), "")
                            .replaceAll(EMAIL_PATTERN, "[EMAIL REDACTED]")
                            .replaceAll(PHONE_PATTERN, "[PHONE REDACTED]")
                            .replaceAll(SSN_PATTERN, "[SSN REDACTED]");
                    // Preserve the original document metadata while returning masked content
                    return new Document(maskedText, document.getMetadata());
                })
                .toList();
    }
}
