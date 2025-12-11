package com.eazybytes.mcpserverremote.model;

// New to Sec7_Chap82: Created TicketRequest record to hold ticket request details
// Unlike in Sec6_Chap69,
// the username parameter must now be passed explicitly because HelpDeskTool no longer has access to the ToolContext.
// This is due to the tooling logic being moved into the GitHub-MCP server.
public record TicketRequest(String issue, String username) {
}
