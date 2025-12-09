package com.turboguys.myaimodelsbot.domain.model

enum class AiModel(val modelId: String, val displayName: String) {
    DEEPSEEK_CHIMERA(
        modelId = "tngtech/deepseek-r1t2-chimera:free",
        displayName = "DeepSeek Chimera"
    ),
    AMAZON_NOVA_LITE(
        modelId = "amazon/nova-2-lite-v1:free",
        displayName = "Amazon Nova Lite"
    ),
    GOOGLE_GEMMA(
        modelId = "google/gemma-3n-e4b-it:free",
        displayName = "Google Gemma"
    )
}
