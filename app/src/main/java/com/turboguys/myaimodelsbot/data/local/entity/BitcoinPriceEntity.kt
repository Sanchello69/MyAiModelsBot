package com.turboguys.myaimodelsbot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bitcoin_prices")
data class BitcoinPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val price: Double,
    val timestamp: Long,
    val analysis: String? = null // AI анализ изменений
)
