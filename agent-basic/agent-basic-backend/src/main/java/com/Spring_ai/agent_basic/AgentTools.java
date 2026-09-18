package com.Spring_ai.agent_basic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AgentTools {

	@Tool(description = "get me the current date and time")
	public String getCurrentDateAndTime() {
		return  LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
	}
	
	@Tool(description = "Converts US Dollars (USD) to Indian Rupees (INR)")
    public String usdToInd(@ToolParam(description = "Amount in USD to convert") int amt) {
        int exchange = 83;
        int ind = amt * exchange;
        return String.format("%d USD is equal to %d INR", amt, ind);
    }
}
