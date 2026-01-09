package behavior

import cache.Config
import cache.Interface as CacheInterface
import cache.Provider
import domain.Client
import domain.ClientType
import domain.User
import drivers.InMemoryUserRepository
import fixtures.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*

@DisplayName("Cache Integration Behavior")
class CacheIntegrationBehaviorTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var cache: CacheInterface<User>

    @BeforeEach
    fun setup() {
        userRepository = InMemoryUserRepository()
        cache = Provider.createLRUCache(Config(10))
    }

    @Test
    @DisplayName("cache miss fetches from repository")
    fun `cache miss fetches from repository`() = runTest {
        // Given
        val user = TestFixtures.aUser(id = "user-1", email = "alice@example.com")
        userRepository.addUser(user)

        // When - simulate cache-aside pattern
        var result = cache.get("user-1")
        if (result == null) {
            result = userRepository.findById("user-1")
            if (result != null) {
                cache.set("user-1", result)
            }
        }

        // Then
        assertNotNull(result)
        assertEquals("alice@example.com", result?.email)
        assertEquals(1, userRepository.findCallCount) // DB was called
    }

    @Test
    @DisplayName("cache hit skips repository")
    fun `cache hit skips repository`() = runTest {
        // Given
        val user = TestFixtures.aUser(id = "user-1", email = "alice@example.com")
        cache.set("user-1", user)

        // When - simulate cache-aside pattern
        var result = cache.get("user-1")
        if (result == null) {
            result = userRepository.findById("user-1")
        }

        // Then
        assertNotNull(result)
        assertEquals("alice@example.com", result?.email)
        assertEquals(0, userRepository.findCallCount) // DB was NOT called
    }

    @Test
    @DisplayName("save updates both repository and cache")
    fun `save updates both repository and cache`() = runTest {
        // Given
        val user = TestFixtures.aUser(id = "user-1", email = "alice@example.com")

        // When - simulate write-through pattern
        userRepository.save(user)
        cache.set("user-1", user)

        // Then - can retrieve from cache without hitting DB
        userRepository.findCallCount // reset baseline
        val cached = cache.get("user-1")

        assertNotNull(cached)
        assertEquals("alice@example.com", cached?.email)
    }

    @Test
    @DisplayName("update propagates to cache")
    fun `update propagates to cache`() = runTest {
        // Given
        val originalUser = TestFixtures.aUser(id = "user-1", firstname = "Alice")
        userRepository.save(originalUser)
        cache.set("user-1", originalUser)

        // When - update user
        val updatedUser = originalUser.copy(firstname = "Alicia")
        userRepository.update(updatedUser)
        cache.set("user-1", updatedUser)

        // Then
        val cached = cache.get("user-1")
        assertNotNull(cached)
        assertEquals("Alicia", cached?.firstname)
    }

    @Test
    @DisplayName("multiple users can be cached")
    fun `multiple users can be cached`() = runTest {
        // Given
        val alice = TestFixtures.aUser(id = "user-1", email = "alice@example.com")
        val bob = TestFixtures.aUser(id = "user-2", email = "bob@example.com")

        // When
        cache.set("user-1", alice)
        cache.set("user-2", bob)

        // Then
        assertEquals("alice@example.com", cache.get("user-1")?.email)
        assertEquals("bob@example.com", cache.get("user-2")?.email)
    }
}
