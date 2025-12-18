package com.turboguys.myaimodelsbot.presentation.bitcoin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turboguys.myaimodelsbot.data.local.entity.BitcoinPriceEntity
import com.turboguys.myaimodelsbot.domain.repository.BitcoinRepository
import com.turboguys.myaimodelsbot.service.ACTION_BITCOIN_UPDATE
import com.turboguys.myaimodelsbot.service.BitcoinMonitorService
import com.turboguys.myaimodelsbot.service.EXTRA_ANALYSIS
import com.turboguys.myaimodelsbot.service.EXTRA_PRICE
import com.turboguys.myaimodelsbot.service.EXTRA_TIMESTAMP
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BitcoinMonitorUiState(
    val isMonitoring: Boolean = false,
    val currentPrice: Double? = null,
    val latestAnalysis: String? = null,
    val priceHistory: List<BitcoinPriceEntity> = emptyList(),
    val lastUpdateTimestamp: Long? = null
)

class BitcoinMonitorViewModel(
    private val bitcoinRepository: BitcoinRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BitcoinMonitorUiState())
    val uiState: StateFlow<BitcoinMonitorUiState> = _uiState.asStateFlow()

    private val bitcoinUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BITCOIN_UPDATE) {
                val price = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
                val analysis = intent.getStringExtra(EXTRA_ANALYSIS)
                val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0L)

                _uiState.update {
                    it.copy(
                        currentPrice = price,
                        latestAnalysis = analysis,
                        lastUpdateTimestamp = timestamp
                    )
                }
            }
        }
    }

    init {
        // Регистрируем receiver для получения обновлений
        val filter = IntentFilter(ACTION_BITCOIN_UPDATE)
        context.registerReceiver(bitcoinUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        // Загружаем историю
        loadPriceHistory()
    }

    private fun loadPriceHistory() {
        viewModelScope.launch {
            bitcoinRepository.getLatestPrices(20).collect { history ->
                _uiState.update { it.copy(priceHistory = history) }
            }
        }
    }

    fun startMonitoring() {
        BitcoinMonitorService.start(context)
        _uiState.update { it.copy(isMonitoring = true) }
    }

    fun stopMonitoring() {
        BitcoinMonitorService.stop(context)
        _uiState.update { it.copy(isMonitoring = false) }
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(bitcoinUpdateReceiver)
    }
}
