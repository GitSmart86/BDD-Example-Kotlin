package com.speechify.cache

class LRUCacheImpl<T>(private val maxSize: Int) : LRUCacheInterface<T> {
    // Use LinkedHashMap with accessOrder=true for automatic LRU ordering
    // When accessOrder is true, the order of iteration is the order in which entries were last accessed
    private val cache = object : LinkedHashMap<String, T>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?): Boolean {
            return size > maxSize
        }
    }

    override fun get(key: String): T? {
        return cache[key]
    }

    override fun set(key: String, value: T) {
        cache[key] = value
    }
}
