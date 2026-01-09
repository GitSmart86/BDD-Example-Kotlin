package com.speechify.policy

import com.speechify.domain.Client

interface CreditPolicy {
    fun calculateCreditLimit(client: Client): CreditLimit
}

data class CreditLimit(
    val hasLimit: Boolean,
    val amount: Double
)
