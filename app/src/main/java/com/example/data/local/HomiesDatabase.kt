package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        StoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HomiesDatabase : RoomDatabase() {
    abstract fun homiesDao(): HomiesDao

    companion object {
        @Volatile
        private var INSTANCE: HomiesDatabase? = null

        fun getDatabase(context: Context): HomiesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomiesDatabase::class.java,
                    "homies_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
