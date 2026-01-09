package repository

import domain.User
import domain.Client

/**
 * Repository interface for user persistence operations.
 *
 * All operations are suspend functions for coroutine support.
 * Implementations may include caching, file I/O, or database access.
 */
interface UserRepository {
    /** @return User with matching id, or null if not found */
    suspend fun findById(id: String): User?

    /** @return User with matching email, or null if not found */
    suspend fun findByEmail(email: String): User?

    /** @return All users, empty list if none exist */
    suspend fun findAll(): List<User>

    /** @return true if save succeeded */
    suspend fun save(user: User): Boolean

    /** @return true if update succeeded (user existed) */
    suspend fun update(user: User): Boolean

    /** @return true if a user with this email exists */
    suspend fun existsByEmail(email: String): Boolean
}

/**
 * Repository interface for client persistence operations.
 *
 * Clients are typically read-only reference data.
 */
interface ClientRepository {
    /** @return Client with matching id, or null if not found */
    suspend fun findById(id: String): Client?

    /** @return All clients */
    suspend fun findAll(): List<Client>
}
