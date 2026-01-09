package com.speechify.policy

import com.speechify.domain.Client
import com.speechify.domain.ClientType

class DefaultCreditPolicy : CreditPolicy {

    companion object {
        private const val IMPORTANT_CLIENT_LIMIT = 20_000.0
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
