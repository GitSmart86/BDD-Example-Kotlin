package com.speechify.repository

import com.speechify.domain.User
import com.speechify.domain.Client

interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun save(user: User): Boolean
    fun update(user: User): Boolean
    fun existsByEmail(email: String): Boolean
}

interface ClientRepository {
    fun findById(id: String): Client?
    fun findAll(): List<Client>
}
