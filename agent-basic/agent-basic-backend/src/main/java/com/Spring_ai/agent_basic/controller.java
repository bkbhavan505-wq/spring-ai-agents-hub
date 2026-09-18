package com.Spring_ai.agent_basic;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class controller {
   
	private AgentTools agentTools;
	
    private final ChatClient chatClient;

    public controller(ChatClient.Builder chatClientBuilder) {
        this.agentTools = new AgentTools();
		this.chatClient = chatClientBuilder
        		.defaultSystem("use the tools whenever it is nessecary")
        		.defaultTools(agentTools)
        		.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
        		.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "give me a spring ai project idea") String message,
    		@RequestParam(defaultValue = "user-session-1") String conversId) 
    {
        return this.chatClient.prompt()
                .user(message)
                .advisors(a-> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,conversId))
                .call()
                .content();
    }
}
