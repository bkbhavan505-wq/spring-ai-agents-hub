package com.Spring_ai.agent_basic.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ragService {

    private static final Logger log = LoggerFactory.getLogger(ragService.class);

    private final VectorStore vectorStore;

    @Value("classpath:docs/project.pdf")
    private Resource pdfResource;

    public ragService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void ingestPdfOnStartup() {
        try {
            if (!pdfResource.exists()) {
                log.warn("PDF file not found in classpath:docs/project.pdf. Skipping auto-ingest.");
                return;
            }

            log.info("Loading PDF document: {}", pdfResource.getFilename());

            // 1. Extract text using PDFBox 3.x Loader
            String pdfText;
            try (InputStream is = pdfResource.getInputStream();
                 PDDocument document = Loader.loadPDF(is.readAllBytes())) {

                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document);
                log.info("Extracted text from PDF ({} pages).", document.getNumberOfPages());
            }

            if (pdfText == null || pdfText.isBlank()) {
                log.warn("PDF content is empty. Skipping vector ingestion.");
                return;
            }

            // 2. Wrap into Spring AI Document
            Document doc = new Document(pdfText, Map.of("source", "project.pdf"));

            // 3. Chunk the document
            TokenTextSplitter splitter = new TokenTextSplitter(800, 350, 5, 10000, true);
            List<Document> chunks = splitter.apply(List.of(doc));
            log.info("Split PDF into {} chunks. Sending to Ollama for embedding...", chunks.size());

            // 4. Ingest into VectorStore
            vectorStore.accept(chunks);
            log.info(">> Successfully ingested PDF into VectorStore!");

        } catch (Exception e) {
            log.error("Failed to ingest PDF: {}", e.getMessage(), e);
        }
    }

    public List<Document> search(String query, int topK, double threshold) {
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build()
        );
    }
}