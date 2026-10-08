package com.melonityhub.sublearn.core.data

import android.content.Context
import androidx.room.Room
import com.melonityhub.sublearn.core.data.db.AppDatabase
import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Room + FTS4 behaviour of My Words, run on an in-memory database (Robolectric). */
@RunWith(RobolectricTestRunner::class)
class MyWordsDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: MyWordsRepository

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication() as Context
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        repo = MyWordsRepository(db.myWords(), clock = { 1_000L })
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addIsCaseInsensitiveAndKeepsOneRowPerTerm() = runBlocking {
        val first = repo.add("Hello", "سلام", null, "Movie")
        val second = repo.add("hello", null, "a context line", null)
        assertEquals(first?.id, second?.id)
        assertEquals(1, db.myWords().allTerms().size)
        assertEquals("سلام", repo.observe("hello").first().single().translation)
        assertEquals("a context line", repo.observe("hello").first().single().contextSentence)
    }

    @Test
    fun ftsSearchMatchesPrefixesAndIgnoresBlankQueries() = runBlocking {
        repo.add("interesting", "جالب", null, null)
        repo.add("banana", null, null, null)
        assertEquals(listOf("interesting"), repo.observe("inter").first().map { it.term })
        assertEquals(2, repo.observe("").first().size)
        assertEquals(0, repo.observe("zzz").first().size)
    }

    @Test
    fun removeAndKnownFlagWork() = runBlocking {
        val word = repo.add("apple", null, null, null)!!
        repo.setKnown(word.id, true)
        assertTrue(repo.observe("").first().single().markedKnown)
        repo.remove(word.id)
        assertFalse(repo.contains("apple"))
        assertEquals(emptySet<String>(), repo.savedTerms())
    }
}
