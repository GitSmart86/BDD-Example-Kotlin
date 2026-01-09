package behavior

import dsl.CacheTestDSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("Cache Behavior")
class CacheBehaviorTest {

    private lateinit var dsl: CacheTestDSL<String>

    @BeforeEach
    fun setup() {
        dsl = CacheTestDSL()
    }

    @Test
    @DisplayName("stores and retrieves items")
    fun `stores and retrieves items`() {
        // Given
        dsl.givenCacheWithCapacity(10)

        // When
        dsl.whenStoringItem("user-1", "Alice")

        // Then
        dsl.thenItemCanBeRetrieved("user-1", "Alice")
    }

    @Test
    @DisplayName("returns null for non-existent key")
    fun `returns null for non-existent key`() {
        // Given
        dsl.givenCacheWithCapacity(10)
        dsl.givenCachedItem("user-1", "Alice")

        // When
        dsl.whenAccessingItem("non-existent")

        // Then
        dsl.thenRetrievedValueIsNull()
    }

    @Test
    @DisplayName("evicts least recently used when capacity exceeded")
    fun `evicts least recently used when capacity exceeded`() {
        // Given
        dsl.givenCacheWithCapacity(2)
        dsl.givenCachedItem("user-A", "Alice")
        dsl.givenCachedItem("user-B", "Bob")
        dsl.givenItemWasAccessed("user-A") // A is now recently used

        // When - add third item, exceeds capacity
        dsl.whenStoringItem("user-C", "Charlie")

        // Then - B should be evicted (least recently used)
        dsl.thenItemCanBeRetrieved("user-A", "Alice")
        dsl.thenItemCanBeRetrieved("user-C", "Charlie")
        dsl.thenItemIsNotInCache("user-B")
    }

    @Test
    @DisplayName("update does not cause eviction")
    fun `update does not cause eviction`() {
        // Given
        dsl.givenCacheWithCapacity(2)
        dsl.givenCachedItem("user-A", "Alice")
        dsl.givenCachedItem("user-B", "Bob")

        // When - update existing item
        dsl.whenUpdatingItem("user-A", "Alice Updated")

        // Then - both items still present
        dsl.thenItemCanBeRetrieved("user-A", "Alice Updated")
        dsl.thenItemCanBeRetrieved("user-B", "Bob")
    }

    @Test
    @DisplayName("get refreshes access time")
    fun `get refreshes access time`() {
        // Given
        dsl.givenCacheWithCapacity(2)
        dsl.givenCachedItem("user-A", "Alice")
        dsl.givenCachedItem("user-B", "Bob")

        // When - access A (making B the LRU), then add C
        dsl.whenAccessingItem("user-A")
        dsl.whenStoringItem("user-C", "Charlie")

        // Then - B should be evicted, not A
        dsl.thenItemCanBeRetrieved("user-A", "Alice")
        dsl.thenItemCanBeRetrieved("user-C", "Charlie")
        dsl.thenItemIsNotInCache("user-B")
    }

    @Test
    @DisplayName("can re-add evicted items")
    fun `can re-add evicted items`() {
        // Given
        dsl.givenCacheWithCapacity(1)
        dsl.givenCachedItem("user-A", "Alice")

        // When - add B (evicts A), then re-add A (evicts B)
        dsl.whenStoringItem("user-B", "Bob")
        dsl.whenStoringItem("user-A", "Alice Again")

        // Then
        dsl.thenItemCanBeRetrieved("user-A", "Alice Again")
        dsl.thenItemIsNotInCache("user-B")
    }
}
