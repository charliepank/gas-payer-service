package com.gaspayer.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigInteger

data class FundWalletRequest(
    @field:NotBlank(message = "Wallet address is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{40}$",
        message = "Invalid wallet address format"
    )
    val walletAddress: String,

    @field:Positive(message = "Amount must be positive")
    val totalAmountNeededWei: BigInteger
)