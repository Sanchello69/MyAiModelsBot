package com.turboguys.myaimodelsbot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.turboguys.myaimodelsbot.data.local.entity.BitcoinPriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitcoinPriceDao {
    @Insert
    suspend fun insert(price: BitcoinPriceEntity): Long

    @Query("SELECT * FROM bitcoin_prices ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): BitcoinPriceEntity?

    @Query("SELECT * FROM bitcoin_prices ORDER BY timestamp DESC LIMIT 2")
    suspend fun getLatestTwo(): List<BitcoinPriceEntity>

    @Query("SELECT * FROM bitcoin_prices ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestPrices(limit: Int = 10): Flow<List<BitcoinPriceEntity>>

    @Query("SELECT * FROM bitcoin_prices ORDER BY timestamp DESC")
    fun getAllPrices(): Flow<List<BitcoinPriceEntity>>

    @Query("DELETE FROM bitcoin_prices")
    suspend fun clearAll()
}
