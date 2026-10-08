package com.melonityhub.sublearn.core.translation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TranslationCacheTest {

    @Test
    fun storesAndEvictsLeastRecentlyUsed() {
        val cache = TranslationCache(capacity = 2)
        cache.store("a", "1")
        cache.store("b", "2")
        assertEquals("1", cache.lookup("a")) // touch a, so b becomes the eldest
        cache.store("c", "3")
        assertNull(cache.lookup("b"))
        assertEquals("1", cache.lookup("a"))
        assertEquals("3", cache.lookup("c"))
        assertEquals(2, cache.size())
    }
}
