package com.turboguys.myaimodelsbot.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.turboguys.myaimodelsbot.data.local.dao.MessageDao
import com.turboguys.myaimodelsbot.data.local.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
