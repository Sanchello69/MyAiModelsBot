package com.turboguys.myaimodelsbot.domain.repository

import com.turboguys.myaimodelsbot.data.local.entity.BitcoinPriceEntity
import kotlinx.coroutines.flow.Flow

interface BitcoinRepository {
    suspend fun saveBitcoinPrice(price: Double, timestamp: Long, analysis: String? = null): Long
    suspend fun getLatestPrice(): BitcoinPriceEntity?
    suspend fun getLatestTwoPrices(): List<BitcoinPriceEntity>
    fun getLatestPrices(limit: Int): Flow<List<BitcoinPriceEntity>>
    fun getAllPrices(): Flow<List<BitcoinPriceEntity>>
    suspend fun fetchCurrentBitcoinPrice(): Result<Double>
}
