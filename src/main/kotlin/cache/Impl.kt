package cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coroutine-safe LRU cache implementation using Mutex.
 * Safe for concurrent access from multiple coroutines.
 */
class Impl<T>(private val maxSize: Int) : Interface<T> {
    private val mutex = Mutex()

    // Use LinkedHashMap with accessOrder=true for automatic LRU ordering
    private val cache = object : LinkedHashMap<String, T>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?): Boolean {
            return size > maxSize
        }
    }

    override suspend fun get(key: String): T? = mutex.withLock {
        cache[key]
    }

    override suspend fun set(key: String, value: T) = mutex.withLock {
        cache[key] = value
    }
}
