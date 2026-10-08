package com.melonityhub.sublearn.core.data.repo

import com.melonityhub.sublearn.core.data.db.PlaybackStateDao
import com.melonityhub.sublearn.core.data.db.PlaybackStateEntity
import com.melonityhub.sublearn.core.data.db.RecentVideoDao
import com.melonityhub.sublearn.core.data.db.RecentVideoEntity
import com.melonityhub.sublearn.core.model.MediaSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A recently opened video for Home (APP-2). */
data class RecentVideo(
    val uri: String,
    val title: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val lastOpenedAt: Long,
) {
    /** Fraction watched, 0..1. */
    val progress: Float
        get() = if (durationMs <= 0L) 0f else (lastPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
}

class RecentVideosRepository(
    private val dao: RecentVideoDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeRecent(limit: Int = 20): Flow<List<RecentVideo>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toModel() } }

    /** Records that [source] was opened now, keeping the last known position and duration. */
    suspend fun touch(source: MediaSource) {
        val existing = dao.find(source.uri)
        dao.upsert(
            RecentVideoEntity(
                uri = source.uri,
                title = source.title.ifBlank { existing?.title ?: source.uri.substringAfterLast('/') },
                durationMs = existing?.durationMs ?: 0L,
                lastPositionMs = existing?.lastPositionMs ?: 0L,
                lastOpenedAt = clock(),
            ),
        )
    }

    suspend fun updateProgress(uri: String, positionMs: Long, durationMs: Long) {
        val existing = dao.find(uri) ?: return
        dao.upsert(existing.copy(lastPositionMs = positionMs, durationMs = durationMs.coerceAtLeast(0L)))
    }

    suspend fun remove(uri: String) = dao.delete(uri)
}

/** Saved selections per video so the player can restore them after process death (ENG-8). */
data class PlaybackSnapshot(
    val positionMs: Long,
    val audioTrackId: String?,
    val learningTrackId: String?,
    val translationTrackId: String?,
)

class PlaybackStateRepository(
    private val dao: PlaybackStateDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun load(uri: String): PlaybackSnapshot? = dao.find(uri)?.let {
        PlaybackSnapshot(it.positionMs, it.audioTrackId, it.learningTrackId, it.translationTrackId)
    }

    suspend fun save(uri: String, snapshot: PlaybackSnapshot) {
        dao.upsert(
            PlaybackStateEntity(
                uri = uri,
                positionMs = snapshot.positionMs.coerceAtLeast(0L),
                audioTrackId = snapshot.audioTrackId,
                learningTrackId = snapshot.learningTrackId,
                translationTrackId = snapshot.translationTrackId,
                updatedAt = clock(),
            ),
        )
    }
}

private fun RecentVideoEntity.toModel() = RecentVideo(uri, title, durationMs, lastPositionMs, lastOpenedAt)
