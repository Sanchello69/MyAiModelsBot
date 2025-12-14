package com.turboguys.myaimodelsbot.data.local

import android.content.Context
import android.content.SharedPreferences
import com.turboguys.myaimodelsbot.domain.model.AiModel

class UserPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSelectedModel(model: AiModel) {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_MODEL, model.name)
            .apply()
    }

    fun getSelectedModel(): AiModel {
        val modelName = sharedPreferences.getString(KEY_SELECTED_MODEL, null)
        return if (modelName != null) {
            try {
                AiModel.valueOf(modelName)
            } catch (e: IllegalArgumentException) {
                AiModel.DEEPSEEK_CHIMERA // Модель по умолчанию
            }
        } else {
            AiModel.DEEPSEEK_CHIMERA // Модель по умолчанию
        }
    }

    fun saveMaxTokens(maxTokens: Int) {
        sharedPreferences.edit()
            .putInt(KEY_MAX_TOKENS, maxTokens)
            .apply()
    }

    fun getMaxTokens(): Int {
        return sharedPreferences.getInt(KEY_MAX_TOKENS, 0)
    }

    fun saveCompressionEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_COMPRESSION_ENABLED, enabled)
            .apply()
    }

    fun getCompressionEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_COMPRESSION_ENABLED, false)
    }

    companion object {
        private const val PREFS_NAME = "ai_chat_preferences"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_COMPRESSION_ENABLED = "compression_enabled"
    }
}
