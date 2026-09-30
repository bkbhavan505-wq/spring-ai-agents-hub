package com.Spring_ai.agent_basic;

import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgentBasicApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentBasicApplication.class, args);
	}

	@Bean
	public VectorStore vectorStore(EmbeddingModel model) {
		return SimpleVectorStore.builder(model).build();
	}
}
