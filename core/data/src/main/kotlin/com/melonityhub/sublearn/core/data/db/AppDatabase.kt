package com.melonityhub.sublearn.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RecentVideoEntity::class,
        PlaybackStateEntity::class,
        MyWordEntity::class,
        MyWordFtsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentVideos(): RecentVideoDao
    abstract fun playbackState(): PlaybackStateDao
    abstract fun myWords(): MyWordDao
}
