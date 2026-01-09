package policy

import domain.Client

/**
 * Strategy interface for calculating user credit limits.
 *
 * Implementations define credit policies based on client classification.
 * Uses the Strategy pattern to allow swappable business rules.
 *
 * @see CreditLimit for the return contract
 */
interface UserCredits {
    /**
     * Calculates credit limit based on client's classification.
     *
     * @param client The client whose policy determines the limit
     * @return [CreditLimit] with limit applicability and amount
     */
    fun calculateCreditLimit(client: Client): CreditLimit
}

/**
 * Credit limit result from policy calculation.
 *
 * @property hasLimit Whether credit limits apply (false for VIP clients)
 * @property amount Credit limit amount (0.0 when hasLimit is false)
 */
data class CreditLimit(
    val hasLimit: Boolean,
    val amount: Double
)
