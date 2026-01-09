package service

import domain.User
import java.time.LocalDate

interface UserService {
    suspend fun addUser(request: AddUserRequest): AddUserResult
    suspend fun updateUser(user: User): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun getAllUsers(): List<User>
}

data class AddUserRequest(
    val firstname: String,
    val surname: String,
    val email: String,
    val dateOfBirth: LocalDate?,
    val clientId: String
)

sealed class AddUserResult {
    data class Success(val user: User) : AddUserResult()
    data class ValidationError(val reason: String) : AddUserResult()
    object ClientNotFound : AddUserResult()
    object DuplicateEmail : AddUserResult()
}
