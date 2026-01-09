package domain

import java.time.LocalDate

/**
 * Core domain entity representing a user in the system.
 *
 * @property id Unique identifier (UUID)
 * @property client The client organization this user belongs to
 * @property dateOfBirth User's birth date for age validation (null if unknown)
 * @property email Unique email address
 * @property firstname User's first name
 * @property surname User's surname
 * @property hasCreditLimit Whether credit limits apply to this user
 * @property creditLimit Maximum credit amount (0.0 if no limit applies)
 */
data class User(
    val id: String,
    val client: Client,
    val dateOfBirth: LocalDate?,
    val email: String,
    val firstname: String,
    val surname: String,
    val hasCreditLimit: Boolean,
    val creditLimit: Double
)

/**
 * Client organization that users belong to.
 *
 * Client type determines credit policy applied to its users.
 *
 * @property id Unique identifier
 * @property name Display name of the client organization
 * @property type Classification determining credit policy
 */
data class Client(
    val id: String,
    val name: String,
    val type: ClientType
)

/**
 * Client classification determining credit policy.
 *
 * @property VERY_IMPORTANT No credit limit applied
 * @property IMPORTANT Double the base credit limit
 * @property REGULAR Standard credit limit applies
 */
enum class ClientType {
    VERY_IMPORTANT,
    IMPORTANT,
    REGULAR
}
