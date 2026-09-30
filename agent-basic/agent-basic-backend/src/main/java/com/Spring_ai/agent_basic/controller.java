package com.Spring_ai.agent_basic;

import com.Spring_ai.agent_basic.Service.ragService;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@CrossOrigin(origins = "*")
@RestController
public class controller {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ragService ragService;

    public controller(
            ChatClient.Builder builder,
            AgentTools agentTools,
            VectorStore vectorStore,
            ragService ragService) {

        this.vectorStore = vectorStore;
        this.ragService = ragService;

        this.chatClient = builder
                .defaultSystem("You are a helpful AI assistant. Keep responses grounded, concise, and accurate based on provided context.")
                .defaultTools(agentTools)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(1)
                                                .similarityThreshold(0.4)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    /**
     * Streams output token-by-token using Server-Sent Events (SSE).
     * Perceived latency drops from ~15s to ~1.5s.
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam(defaultValue = "Hello") String message,
            @RequestParam(defaultValue = "user-session-1") String conversationId) {

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .stream()     // Reactive stream mode
                .content();   // Emits Flux<String> token stream
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(defaultValue = "Hello") String message,
            @RequestParam(defaultValue = "user-session-1") String conversationId) {

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();
    }

    @GetMapping("/rag/debug")
    public List<Document> debugRag(@RequestParam(defaultValue = "project") String query) {
        return this.ragService.search(query, 2, 0.4);
    }
}