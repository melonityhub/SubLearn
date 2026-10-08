package com.melonityhub.sublearn.core.data

import com.melonityhub.sublearn.core.data.repo.MyWordsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure rules of My Words. Database behaviour (FTS and uniqueness) is covered by MyWordsDatabaseTest. */
class MyWordsRepositoryTest {

    @Test
    fun normalize_lowercasesTrimsAndDropsZwnj() {
        assertEquals("book", MyWordsRepository.normalize("  Book "))
        assertEquals("میخواهم", MyWordsRepository.normalize("می\u200Cخواهم"))
    }

    @Test
    fun ftsMatch_quotesEveryTermAndIgnoresSyntax() {
        assertEquals("\"hel*\" \"wor*\"", MyWordsRepository.ftsMatchFor("hel wor"))
        assertEquals("\"NEAR*\"", MyWordsRepository.ftsMatchFor("\"NEAR\"*"))
        assertNull(MyWordsRepository.ftsMatchFor("   "))
    }
}
