package repository

import cache.Interface as CacheInterface
import domain.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * Decorator that adds caching to any [UserRepository] implementation.
 *
 * Uses cache-aside (lazy loading) for reads and write-through for writes.
 * Coroutine-safe with Mutex-protected email-to-id mapping.
 *
 * @param delegate The underlying repository to cache
 * @param cache LRU cache for User objects by id
 * @param emailToIdCache Secondary cache mapping email to user id
 */
class CachedUser(
    private val delegate: UserRepository,
    private val cache: CacheInterface<User>,
    private val emailToIdCache: CacheInterface<String> = createSimpleCache()
) : UserRepository {

    private val logger = LoggerFactory.getLogger(CachedUser::class.java)

    companion object {
        private fun createSimpleCache(): CacheInterface<String> {
            return object : CacheInterface<String> {
                private val mutex = Mutex()
                private val map = mutableMapOf<String, String>()
                override suspend fun get(key: String): String? = mutex.withLock { map[key] }
                override suspend fun set(key: String, value: String) = mutex.withLock { map[key] = value }
            }
        }
    }

    override suspend fun findById(id: String): User? {
        // Try cache first
        cache.get(id)?.let {
            return it
        }

        // Cache miss - fetch from delegate and cache
        logger.debug("Cache MISS for user id={}", id)
        return delegate.findById(id)?.also { user ->
            cache.set(id, user)
            emailToIdCache.set(user.email, user.id)
            logger.debug("Cached user: id={}", id)
        }
    }

    override suspend fun findByEmail(email: String): User? {
        // Check if we have the ID cached for this email
        emailToIdCache.get(email)?.let { id ->
            cache.get(id)?.let {
                return it
            }
        }

        // Cache miss - fetch from delegate and cache
        logger.debug("Cache MISS for user email={}", email)
        return delegate.findByEmail(email)?.also { user ->
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
            logger.debug("Cached user: email={}", email)
        }
    }

    override suspend fun findAll(): List<User> {
        // For findAll, we always go to delegate but cache the results
        return delegate.findAll().also { users ->
            users.forEach { user ->
                cache.set(user.id, user)
                emailToIdCache.set(user.email, user.id)
            }
            logger.debug("Cached {} users", users.size)
        }
    }

    override suspend fun save(user: User): Boolean {
        // Write-through: save to delegate first, then cache
        val result = delegate.save(user)
        if (result) {
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
            logger.debug("Write-through: cached saved user id={}", user.id)
        }
        return result
    }

    override suspend fun update(user: User): Boolean {
        // Write-through: update delegate first, then cache
        val result = delegate.update(user)
        if (result) {
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
            logger.debug("Write-through: cached updated user id={}", user.id)
        }
        return result
    }

    override suspend fun existsByEmail(email: String): Boolean {
        // Check email cache first
        if (emailToIdCache.get(email) != null) {
            return true
        }
        // Fall back to delegate
        logger.debug("Cache MISS for email existence check: {}", email)
        return delegate.existsByEmail(email)
    }
}
