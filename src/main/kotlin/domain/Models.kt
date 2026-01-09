package domain

import java.time.LocalDate

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

data class Client(
    val id: String,
    val name: String,
    val type: ClientType
)

enum class ClientType {
    VERY_IMPORTANT,
    IMPORTANT,
    REGULAR
}
