package cache

/**
 * Thread-safe LRU Cache implementation.
 *
 * Uses LinkedHashMap with accessOrder=true for automatic LRU ordering,
 * wrapped with synchronized access for thread safety.
 *
 * Safe for both sync and async (coroutine) contexts.
 */
class Impl<T>(private val maxSize: Int) : Interface<T> {
    private val lock = Any()
    private val cache = object : LinkedHashMap<String, T>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?): Boolean {
            return size > maxSize
        }
    }

    override fun get(key: String): T? = synchronized(lock) {
        cache[key]
    }

    override fun set(key: String, value: T): Unit = synchronized(lock) {
        cache[key] = value
    }
}
