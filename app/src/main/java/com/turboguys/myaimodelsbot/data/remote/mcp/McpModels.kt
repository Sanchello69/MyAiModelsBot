package com.turboguys.myaimodelsbot.data.remote.mcp

import com.google.gson.annotations.SerializedName

// JSON-RPC 2.0 Request
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: String,
    val method: String,
    val params: Any? = null
)

// JSON-RPC 2.0 Response
data class JsonRpcResponse<T>(
    val jsonrpc: String,
    val id: String?,
    val result: T?,
    val error: JsonRpcError?
)

data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: Any?
)

// MCP Tool
data class McpTool(
    val name: String,
    val description: String?,
    val inputSchema: ToolInputSchema?
)

data class ToolInputSchema(
    val type: String,
    val properties: Map<String, Any>?,
    val required: List<String>?
)

// Response для списка инструментов
data class ListToolsResult(
    val tools: List<McpTool>
)

// Initialize request
data class InitializeParams(
    val protocolVersion: String = "2024-11-05",
    val capabilities: ClientCapabilities = ClientCapabilities(),
    val clientInfo: ClientInfo
)

data class ClientCapabilities(
    val roots: RootsCapability? = null,
    val sampling: SamplingCapability? = null
)

data class RootsCapability(
    val listChanged: Boolean = false
)

data class SamplingCapability(
    @SerializedName("enabled")
    val enabled: Boolean = false
)

data class ClientInfo(
    val name: String,
    val version: String
)

// Initialize response
data class InitializeResult(
    val protocolVersion: String,
    val capabilities: ServerCapabilities,
    val serverInfo: ServerInfo
)

data class ServerCapabilities(
    val tools: ToolsCapability?,
    val prompts: PromptsCapability?,
    val resources: ResourcesCapability?
)

data class ToolsCapability(
    val listChanged: Boolean?
)

data class PromptsCapability(
    val listChanged: Boolean?
)

data class ResourcesCapability(
    val subscribe: Boolean?,
    val listChanged: Boolean?
)

data class ServerInfo(
    val name: String,
    val version: String
)
