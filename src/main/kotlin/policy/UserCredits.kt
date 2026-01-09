package policy

import domain.Client

interface UserCredits {
    fun calculateCreditLimit(client: Client): CreditLimit
}

data class CreditLimit(
    val hasLimit: Boolean,
    val amount: Double
)
