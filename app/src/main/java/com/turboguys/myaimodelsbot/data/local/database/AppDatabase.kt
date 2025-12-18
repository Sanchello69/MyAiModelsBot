package com.turboguys.myaimodelsbot.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.turboguys.myaimodelsbot.data.local.dao.BitcoinPriceDao
import com.turboguys.myaimodelsbot.data.local.dao.MessageDao
import com.turboguys.myaimodelsbot.data.local.entity.BitcoinPriceEntity
import com.turboguys.myaimodelsbot.data.local.entity.MessageEntity

@Database(
    entities = [MessageEntity::class, BitcoinPriceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun bitcoinPriceDao(): BitcoinPriceDao
}
