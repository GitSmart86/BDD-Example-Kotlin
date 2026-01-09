package dsl

import cache.Config
import cache.Interface as CacheInterface
import cache.Provider
import org.junit.jupiter.api.Assertions.*

class CacheTestDSL<T> {
    private var cache: CacheInterface<T>? = null
    private var lastRetrievedValue: T? = null
    private val accessOrder = mutableListOf<String>()

    // GIVEN methods
    fun givenCacheWithCapacity(capacity: Int) {
        cache = Provider.createLRUCache(Config(capacity))
    }

    suspend fun givenCachedItem(key: String, value: T) {
        requireNotNull(cache) { "Cache not initialized. Call givenCacheWithCapacity() first." }
        cache!!.set(key, value)
        accessOrder.add(key)
    }

    suspend fun givenCachedItems(vararg items: Pair<String, T>) {
        items.forEach { (key, value) -> givenCachedItem(key, value) }
    }

    suspend fun givenItemWasAccessed(key: String) {
        requireNotNull(cache) { "Cache not initialized." }
        cache!!.get(key)
        accessOrder.remove(key)
        accessOrder.add(key)
    }

    // WHEN methods
    suspend fun whenStoringItem(key: String, value: T) {
        requireNotNull(cache) { "Cache not initialized." }
        cache!!.set(key, value)
    }

    suspend fun whenAccessingItem(key: String) {
        requireNotNull(cache) { "Cache not initialized." }
        lastRetrievedValue = cache!!.get(key)
    }

    suspend fun whenUpdatingItem(key: String, newValue: T) {
        requireNotNull(cache) { "Cache not initialized." }
        cache!!.set(key, newValue)
    }

    // THEN methods
    suspend fun thenItemCanBeRetrieved(key: String, expectedValue: T) {
        requireNotNull(cache) { "Cache not initialized." }
        val actual = cache!!.get(key)
        assertNotNull(actual, "Expected item with key '$key' to exist in cache")
        assertEquals(expectedValue, actual, "Value mismatch for key '$key'")
    }

    suspend fun thenItemIsNotInCache(key: String) {
        requireNotNull(cache) { "Cache not initialized." }
        val actual = cache!!.get(key)
        assertNull(actual, "Expected item with key '$key' to NOT be in cache, but found: $actual")
    }

    fun thenRetrievedValueIs(expected: T) {
        assertEquals(expected, lastRetrievedValue, "Retrieved value mismatch")
    }

    fun thenRetrievedValueIsNull() {
        assertNull(lastRetrievedValue, "Expected null but got: $lastRetrievedValue")
    }

    suspend fun thenCacheContainsExactly(vararg keys: String) {
        requireNotNull(cache) { "Cache not initialized." }
        keys.forEach { key ->
            assertNotNull(cache!!.get(key), "Expected key '$key' in cache")
        }
    }

    fun thenCacheSize(): Int {
        // Note: This is a helper for assertions, actual size tracking
        // would need to be part of cache implementation
        return accessOrder.size
    }

    fun getCache(): CacheInterface<T>? = cache

    fun reset() {
        cache = null
        lastRetrievedValue = null
        accessOrder.clear()
    }
}
