package policy

import domain.Client
import domain.ClientType

/**
 * Default credit policy implementation.
 *
 * Credit limits by client type:
 * - VERY_IMPORTANT: No limit
 * - IMPORTANT: $20,000
 * - REGULAR: $10,000
 */
class UserCreditsDefault : UserCredits {

    companion object {
        /** Credit limit for IMPORTANT clients */
        private const val IMPORTANT_CLIENT_LIMIT = 20_000.0
        /** Credit limit for REGULAR clients */
        private const val REGULAR_CLIENT_LIMIT = 10_000.0
    }

    override fun calculateCreditLimit(client: Client): CreditLimit {
        return when (client.type) {
            ClientType.VERY_IMPORTANT -> CreditLimit(
                hasLimit = false,
                amount = 0.0
            )
            ClientType.IMPORTANT -> CreditLimit(
                hasLimit = true,
                amount = IMPORTANT_CLIENT_LIMIT
            )
            ClientType.REGULAR -> CreditLimit(
                hasLimit = true,
                amount = REGULAR_CLIENT_LIMIT
            )
        }
    }
}
