package service

import domain.User
import org.slf4j.LoggerFactory
import policy.UserCredits
import repository.ClientRepository
import repository.UserRepository
import java.util.UUID

/**
 * Default implementation of [UserService].
 *
 * Orchestrates user creation with validation, client lookup,
 * credit policy application, and persistence.
 *
 * @param userRepository For user persistence
 * @param clientRepository For client lookup
 * @param creditPolicy Strategy for calculating credit limits
 * @param validator For request validation
 */
class UserDefault(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val creditPolicy: UserCredits,
    private val validator: UserValidator = UserValidator()
) : UserService {

    private val logger = LoggerFactory.getLogger(UserDefault::class.java)

    override suspend fun addUser(request: AddUserRequest): AddUserResult {
        logger.info("Adding user: email={}", request.email)

        // 1. Validate request fields
        validator.validate(request)?.let { error ->
            logger.warn("Validation failed for {}: {}", request.email, error.reason)
            return error
        }

        // 2. Check for duplicate email
        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Duplicate email rejected: {}", request.email)
            return AddUserResult.DuplicateEmail
        }

        // 3. Find the client
        val client = clientRepository.findById(request.clientId)
        if (client == null) {
            logger.warn("Client not found: {}", request.clientId)
            return AddUserResult.ClientNotFound
        }

        // 4. Calculate credit limit based on client type
        val creditLimit = creditPolicy.calculateCreditLimit(client)

        // 5. Create and save the user
        val user = User(
            id = UUID.randomUUID().toString(),
            client = client,
            dateOfBirth = request.dateOfBirth,
            email = request.email,
            firstname = request.firstname,
            surname = request.surname,
            hasCreditLimit = creditLimit.hasLimit,
            creditLimit = creditLimit.amount
        )

        val saved = userRepository.save(user)

        return if (saved) {
            logger.info("User created successfully: id={}, email={}", user.id, user.email)
            AddUserResult.Success(user)
        } else {
            logger.error("Failed to save user: email={}", request.email)
            AddUserResult.ValidationError("Failed to save user")
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return userRepository.update(user).also { success ->
            if (success) {
                logger.info("User updated: id={}", user.id)
            } else {
                logger.warn("User update failed: id={}", user.id)
            }
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override suspend fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
