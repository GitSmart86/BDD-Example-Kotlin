package repository

import domain.User
import domain.Client

interface UserRepository {
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findAll(): List<User>
    suspend fun save(user: User): Boolean
    suspend fun update(user: User): Boolean
    suspend fun existsByEmail(email: String): Boolean
}

interface ClientRepository {
    suspend fun findById(id: String): Client?
    suspend fun findAll(): List<Client>
}
