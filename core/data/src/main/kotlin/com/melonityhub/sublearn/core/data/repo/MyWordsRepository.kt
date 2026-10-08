package com.melonityhub.sublearn.core.data.repo

import com.melonityhub.sublearn.core.data.db.MyWordDao
import com.melonityhub.sublearn.core.data.db.MyWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A saved word as shown in the UI. */
data class MyWord(
    val id: Long,
    val term: String,
    val translation: String?,
    val contextSentence: String?,
    val sourceTitle: String?,
    val markedKnown: Boolean,
)

/** My Words (LRN-1). Terms are stored lowercase and unique; searching uses FTS4 prefix queries. */
class MyWordsRepository(
    private val dao: MyWordDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observe(query: String): Flow<List<MyWord>> {
        val match = ftsMatchFor(query)
        val source = if (match == null) dao.observeAll() else dao.search(match)
        return source.map { rows -> rows.map { it.toModel() } }
    }

    /** Adds [word] (trimmed, lowercased for uniqueness). An existing term keeps its id and gets any new details. */
    suspend fun add(word: String, translation: String?, contextSentence: String?, sourceTitle: String?): MyWord? {
        val display = word.trim()
        if (display.isEmpty()) return null
        val term = normalize(display)
        val existing = dao.findByTerm(term)
        if (existing != null) {
            val updated = existing.copy(
                translation = translation?.takeIf { it.isNotBlank() } ?: existing.translation,
                contextSentence = contextSentence?.takeIf { it.isNotBlank() } ?: existing.contextSentence,
                markedKnown = false,
            )
            dao.update(updated)
            return updated.toModel()
        }
        val entity = MyWordEntity(
            term = term,
            displayTerm = display,
            translation = translation?.takeIf { it.isNotBlank() },
            contextSentence = contextSentence?.takeIf { it.isNotBlank() },
            sourceTitle = sourceTitle?.takeIf { it.isNotBlank() },
            createdAt = clock(),
        )
        val id = dao.insert(entity)
        return entity.copy(id = id).toModel()
    }

    suspend fun remove(id: Long) = dao.deleteById(id)

    suspend fun setKnown(id: Long, known: Boolean) {
        val row = dao.findById(id) ?: return
        dao.update(row.copy(markedKnown = known))
    }

    /** Lowercase terms of every saved word, used by the learning-mode popup pipeline. */
    suspend fun savedTerms(): Set<String> = dao.allTerms().toSet()

    suspend fun contains(word: String): Boolean = dao.findByTerm(normalize(word)) != null

    companion object {
        /** Lowercase, trimmed, with ZWNJ removed so Persian and English keys compare the same way. */
        fun normalize(word: String): String = word.trim().lowercase().replace("\u200C", "")

        /**
         * Builds an FTS4 prefix query from free text, quoting every term so user input can never
         * become FTS syntax. Returns null for blank input.
         */
        fun ftsMatchFor(query: String): String? {
            val terms = query.trim().split(Regex("\\s+"))
                .map { it.replace("\"", "").replace("*", "") }
                .filter { it.isNotEmpty() }
            if (terms.isEmpty()) return null
            return terms.joinToString(" ") { "\"$it*\"" }
        }
    }
}

private fun MyWordEntity.toModel() = MyWord(
    id = id,
    term = displayTerm,
    translation = translation,
    contextSentence = contextSentence,
    sourceTitle = sourceTitle,
    markedKnown = markedKnown,
)
