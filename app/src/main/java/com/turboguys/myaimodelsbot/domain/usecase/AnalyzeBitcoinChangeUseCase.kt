package com.turboguys.myaimodelsbot.domain.usecase

import com.turboguys.myaimodelsbot.data.remote.OpenRouterApiService
import com.turboguys.myaimodelsbot.data.remote.dto.MessageDto
import com.turboguys.myaimodelsbot.data.remote.dto.ChatRequest
import com.turboguys.myaimodelsbot.domain.model.AiModel

class AnalyzeBitcoinChangeUseCase(
    private val apiService: OpenRouterApiService
) {
    suspend operator fun invoke(
        previousPrice: Double,
        currentPrice: Double
    ): Result<String> {
        return try {
            val change = currentPrice - previousPrice
            val changePercent = (change / previousPrice) * 100

            val prompt = """
                Проанализируй изменение курса биткоина:
                - Предыдущее значение: $$previousPrice
                - Текущее значение: $$currentPrice
                - Изменение: $${"%.2f".format(change)} (${
                "%.2f".format(changePercent)
            }%)

                Дай краткий анализ (1-2 предложения):
                - Что произошло с курсом
                - Стоит ли обратить внимание на это изменение
            """.trimIndent()

            val request = ChatRequest(
                model = AiModel.DEEPSEEK_V3.modelId,
                messages = listOf(
                    MessageDto(
                        role = "user",
                        content = prompt
                    )
                ),
                maxTokens = 150
            )

            val response = apiService.sendMessage(request)
            val analysis = response.choices.firstOrNull()?.message?.content
                ?: "Не удалось получить анализ"

            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
