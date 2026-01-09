package com.speechify.service

import com.speechify.domain.User
import com.speechify.policy.UserCredits
import com.speechify.repository.ClientRepository
import com.speechify.repository.UserRepository
import java.util.UUID

class UserDefault(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val creditPolicy: UserCredits,
    private val validator: UserValidator = UserValidator()
) : UserService {

    override fun addUser(request: AddUserRequest): AddUserResult {
        // 1. Validate request fields
        validator.validate(request)?.let { return it }

        // 2. Check for duplicate email
        if (userRepository.existsByEmail(request.email)) {
            return AddUserResult.DuplicateEmail
        }

        // 3. Find the client
        val client = clientRepository.findById(request.clientId)
            ?: return AddUserResult.ClientNotFound

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
            AddUserResult.Success(user)
        } else {
            AddUserResult.ValidationError("Failed to save user")
        }
    }

    override fun updateUser(user: User): Boolean {
        return userRepository.update(user)
    }

    override fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
