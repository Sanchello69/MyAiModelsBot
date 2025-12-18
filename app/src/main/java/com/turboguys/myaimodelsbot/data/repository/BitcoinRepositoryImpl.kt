package com.turboguys.myaimodelsbot.data.repository

import com.turboguys.myaimodelsbot.data.local.dao.BitcoinPriceDao
import com.turboguys.myaimodelsbot.data.local.entity.BitcoinPriceEntity
import com.turboguys.myaimodelsbot.data.remote.mcp.SimpleMcpClient
import com.turboguys.myaimodelsbot.domain.repository.BitcoinRepository
import kotlinx.coroutines.flow.Flow

class BitcoinRepositoryImpl(
    private val bitcoinPriceDao: BitcoinPriceDao,
    private val mcpClient: SimpleMcpClient
) : BitcoinRepository {

    override suspend fun saveBitcoinPrice(
        price: Double,
        timestamp: Long,
        analysis: String?
    ): Long {
        val entity = BitcoinPriceEntity(
            price = price,
            timestamp = timestamp,
            analysis = analysis
        )
        return bitcoinPriceDao.insert(entity)
    }

    override suspend fun getLatestPrice(): BitcoinPriceEntity? {
        return bitcoinPriceDao.getLatest()
    }

    override suspend fun getLatestTwoPrices(): List<BitcoinPriceEntity> {
        return bitcoinPriceDao.getLatestTwo()
    }

    override fun getLatestPrices(limit: Int): Flow<List<BitcoinPriceEntity>> {
        return bitcoinPriceDao.getLatestPrices(limit)
    }

    override fun getAllPrices(): Flow<List<BitcoinPriceEntity>> {
        return bitcoinPriceDao.getAllPrices()
    }

    override suspend fun fetchCurrentBitcoinPrice(): Result<Double> {
        return mcpClient.getBitcoinPrice().map { it.price }
    }
}
