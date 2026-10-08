package com.melonityhub.sublearn.core.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentVideoDao {
    @Query("SELECT * FROM recent_videos ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentVideoEntity>>

    @Query("SELECT * FROM recent_videos WHERE uri = :uri LIMIT 1")
    suspend fun find(uri: String): RecentVideoEntity?

    @Upsert
    suspend fun upsert(entity: RecentVideoEntity)

    @Query("DELETE FROM recent_videos WHERE uri = :uri")
    suspend fun delete(uri: String)
}

@Dao
interface PlaybackStateDao {
    @Query("SELECT * FROM playback_state WHERE uri = :uri LIMIT 1")
    suspend fun find(uri: String): PlaybackStateEntity?

    @Upsert
    suspend fun upsert(entity: PlaybackStateEntity)
}

@Dao
interface MyWordDao {
    @Query("SELECT * FROM my_words ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MyWordEntity>>

    /** [match] is an FTS4 MATCH expression built by the repository (never raw user input). */
    @Query(
        "SELECT * FROM my_words WHERE id IN (SELECT rowid FROM my_words_fts WHERE my_words_fts MATCH :match) " +
            "ORDER BY createdAt DESC",
    )
    fun search(match: String): Flow<List<MyWordEntity>>

    @Query("SELECT * FROM my_words WHERE term = :term LIMIT 1")
    suspend fun findByTerm(term: String): MyWordEntity?

    @Query("SELECT * FROM my_words WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MyWordEntity?

    @Query("SELECT term FROM my_words")
    suspend fun allTerms(): List<String>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MyWordEntity): Long

    @Update
    suspend fun update(entity: MyWordEntity)

    @Query("DELETE FROM my_words WHERE id = :id")
    suspend fun deleteById(id: Long)
}
