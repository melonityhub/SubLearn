package com.melonityhub.sublearn.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

/** A video the user opened, for Home's recent list (APP-2). */
@Entity(tableName = "recent_videos")
data class RecentVideoEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val lastOpenedAt: Long,
)

/** Per-video resume data: position, chosen tracks and layout (ENG-8 process-death restore). */
@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val uri: String,
    val positionMs: Long,
    val audioTrackId: String?,
    val learningTrackId: String?,
    val translationTrackId: String?,
    val updatedAt: Long,
)

/** A saved word (LRN-1, My Words). [term] is lowercase and unique. */
@Entity(tableName = "my_words", indices = [Index(value = ["term"], unique = true)])
data class MyWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val displayTerm: String,
    val translation: String?,
    val contextSentence: String?,
    val sourceTitle: String?,
    val markedKnown: Boolean = false,
    val createdAt: Long,
)

/** Full-text index over My Words (Room FTS4, kept in sync by Room through [MyWordEntity]). */
@Fts4(contentEntity = MyWordEntity::class)
@Entity(tableName = "my_words_fts")
data class MyWordFtsEntity(
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "translation") val translation: String?,
)
