package cache

/**
 *
 * Use the provided com.speechify.LRUCacheProviderTest in `src/test/kotlin/LruCacheTest.kt` to validate your
 * implementation.
 *
 * You may:
 *  - Read online API references for Java standard library or JVM collections.
 * You must not:
 *  - Read guides about how to code an LRU cache.
 */

object Provider {
    fun <T> createLRUCache(options: Config): Interface<T> {
        return Impl(options.maxItemsCount)
    }
}
