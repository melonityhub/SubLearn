package com.melonityhub.sublearn.core.translation

/** Small least-recently-used cache so repeated taps on the same word do not re-run the model. */
class TranslationCache(private val capacity: Int) {
    init {
        require(capacity > 0) { "capacity must be positive" }
    }

    private val map = object : LinkedHashMap<String, String>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > capacity
    }

    @Synchronized
    fun lookup(key: String): String? = map[key]

    @Synchronized
    fun store(key: String, value: String) {
        map[key] = value
    }

    @Synchronized
    fun size(): Int = map.size
}
