package com.turboguys.myaimodelsbot.data.remote

import com.turboguys.myaimodelsbot.data.remote.dto.ChatRequest
import com.turboguys.myaimodelsbot.data.remote.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApiService {

    @POST("chat/completions")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse
}
