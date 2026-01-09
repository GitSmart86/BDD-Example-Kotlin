package service

import domain.User
import java.time.LocalDate

interface UserService {
    fun addUser(request: AddUserRequest): AddUserResult
    fun updateUser(user: User): Boolean
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): List<User>
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
