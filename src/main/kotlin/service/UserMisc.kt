package service

import domain.User
import java.time.LocalDate

/**
 * Core service interface for user management operations.
 *
 * Provides CRUD operations for users with validation, credit policy application,
 * and persistence. All operations are suspend functions for coroutine support.
 *
 * @see AddUserRequest for the input contract
 * @see AddUserResult for possible outcomes
 */
interface UserService {
    /**
     * Creates a new user with validation and credit policy application.
     *
     * @param request The user creation request containing all required fields
     * @return [AddUserResult] indicating success or specific failure reason
     */
    suspend fun addUser(request: AddUserRequest): AddUserResult

    /**
     * Updates an existing user's information.
     *
     * @param user The user with updated fields (must have valid id)
     * @return true if update succeeded, false otherwise
     */
    suspend fun updateUser(user: User): Boolean

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address to search for
     * @return The matching [User] or null if not found
     */
    suspend fun getUserByEmail(email: String): User?

    /**
     * Retrieves all users in the system.
     *
     * @return List of all users, empty list if none exist
     */
    suspend fun getAllUsers(): List<User>
}

/**
 * Request object for creating a new user.
 *
 * @property firstname User's first name (required, non-blank)
 * @property surname User's surname (required, non-blank)
 * @property email User's email address (required, must be unique)
 * @property dateOfBirth User's date of birth (optional, used for age validation)
 * @property clientId ID of the client this user belongs to (must exist)
 */
data class AddUserRequest(
    val firstname: String,
    val surname: String,
    val email: String,
    val dateOfBirth: LocalDate?,
    val clientId: String
)

/**
 * Sealed class representing all possible outcomes of adding a user.
 *
 * Using a sealed class enables exhaustive when expressions and type-safe
 * result handling without exceptions.
 */
sealed class AddUserResult {
    /**
     * User was created successfully.
     *
     * @property user The newly created user with generated ID and computed credit limit
     */
    data class Success(val user: User) : AddUserResult()

    /**
     * User creation failed due to validation error.
     *
     * @property reason Human-readable description of the validation failure
     */
    data class ValidationError(val reason: String) : AddUserResult()

    /**
     * User creation failed because the specified client does not exist.
     */
    object ClientNotFound : AddUserResult()

    /**
     * User creation failed because a user with this email already exists.
     */
    object DuplicateEmail : AddUserResult()
}
