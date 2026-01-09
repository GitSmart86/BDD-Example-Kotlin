package com.speechify.repository

import com.speechify.LRUCacheInterface
import com.speechify.domain.User

/**
 * Decorator that adds caching to any UserRepository implementation.
 * Uses the cache-aside (lazy loading) pattern for reads
 * and write-through pattern for writes.
 */
class CachedUserRepository(
    private val delegate: UserRepository,
    private val cache: LRUCacheInterface<User>,
    private val emailToIdCache: LRUCacheInterface<String> = createSimpleCache()
) : UserRepository {

    companion object {
        private fun createSimpleCache(): LRUCacheInterface<String> {
            return object : LRUCacheInterface<String> {
                private val map = mutableMapOf<String, String>()
                override fun get(key: String): String? = map[key]
                override fun set(key: String, value: String) { map[key] = value }
            }
        }
    }

    override fun findById(id: String): User? {
        // Try cache first
        cache.get(id)?.let { return it }

        // Cache miss - fetch from delegate and cache
        return delegate.findById(id)?.also { user ->
            cache.set(id, user)
            emailToIdCache.set(user.email, user.id)
        }
    }

    override fun findByEmail(email: String): User? {
        // Check if we have the ID cached for this email
        emailToIdCache.get(email)?.let { id ->
            cache.get(id)?.let { return it }
        }

        // Cache miss - fetch from delegate and cache
        return delegate.findByEmail(email)?.also { user ->
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
        }
    }

    override fun findAll(): List<User> {
        // For findAll, we always go to delegate but cache the results
        return delegate.findAll().also { users ->
            users.forEach { user ->
                cache.set(user.id, user)
                emailToIdCache.set(user.email, user.id)
            }
        }
    }

    override fun save(user: User): Boolean {
        // Write-through: save to delegate first, then cache
        val result = delegate.save(user)
        if (result) {
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
        }
        return result
    }

    override fun update(user: User): Boolean {
        // Write-through: update delegate first, then cache
        val result = delegate.update(user)
        if (result) {
            cache.set(user.id, user)
            emailToIdCache.set(user.email, user.id)
        }
        return result
    }

    override fun existsByEmail(email: String): Boolean {
        // Check email cache first
        if (emailToIdCache.get(email) != null) {
            return true
        }
        // Fall back to delegate
        return delegate.existsByEmail(email)
    }
}
