import com.speechify.CacheLimitsConfig
import com.speechify.LRUCacheInterface
import org.junit.jupiter.api.Test
import com.speechify.LRUCacheProvider.createLRUCache
import org.junit.jupiter.api.Assertions.*

class LruCacheTest {

    @Test
    fun getShouldReturnValueForExistingKey() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(10))
        lruCache.set("foo", "bar")
        assertEquals("bar", lruCache.get("foo"))
    }

    @Test
    fun getShouldReturnNullForNonExistentKey() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(10))
        lruCache.set("foo", "bar")
        assertNull(lruCache.get("bar"))
        assertNull(lruCache.get(""))
    }

    @Test
    fun getShouldReturnValueForManyExistingKeys() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(10))
        lruCache.set("foo", "foo")
        lruCache.set("baz", "baz")
        assertEquals("foo", lruCache.get("foo"))
        assertEquals("baz", lruCache.get("baz"))
    }

    @Test
    fun getShouldReturnNullForKeyNotFittingMaxItemsCount() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(1))
        lruCache.set("foo", "bar")
        lruCache.set("baz", "bar")
        assertNull(lruCache.get("foo"))
        assertEquals("bar", lruCache.get("baz"))
    }

    @Test
    fun getShouldReturnValueForRecreatedKeyAfterItWasPreviouslyRemoved() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(1))
        lruCache.set("foo", "bar")
        lruCache.set("baz", "bar")
        lruCache.set("foo", "bar")
        assertEquals("bar", lruCache.get("foo"))
        assertNull(lruCache.get("baz"))
    }

    @Test
    fun setShouldRemoveOldestKeyOnReachingMaxItemsCountIfNoGetOrHasBeenUsed() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(1))
        lruCache.set("foo", "bar")
        lruCache.set("baz", "bar")
        assertNull(lruCache.get("foo"))
        assertEquals("bar", lruCache.get("baz"))
    }

    @Test
    fun setShouldReplaceExistingValueAndValuesForAllKeysAreKeptWhenCacheLimitIsReached() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(3))
        lruCache.set("bax", "par")
        lruCache.set("foo", "bar1")
        lruCache.set("foo", "bar2")
        lruCache.set("foo", "bar3")
        lruCache.set("baz", "bar")

        assertEquals("bar3", lruCache.get("foo"))
        assertEquals("par", lruCache.get("bax"))
        assertEquals("bar", lruCache.get("baz"))
    }

    @Test
    fun setShouldRemoveLeastRecentlyUsedKeyOnReachingMaxItemsCount() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(2))
        lruCache.set("foo", "bar")
        lruCache.set("bar", "bar")
        lruCache.get("foo")
        lruCache.set("baz", "bar")

        assertEquals("bar", lruCache.get("foo"))
        assertNull(lruCache.get("bar"))
        assertEquals("bar", lruCache.get("baz"))
    }

    @Test
    fun itemIsConsideredAccessedWhenGetIsCalled() {
        val lruCache: LRUCacheInterface<String> = createLRUCache(CacheLimitsConfig(2))
        lruCache.set("1key", "1value")
        lruCache.set("2key", "2value")

        lruCache.get("1key")
        lruCache.set("3key", "3value")

        assertEquals("1value", lruCache.get("1key"))
    }
}
