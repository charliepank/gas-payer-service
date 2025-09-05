package com.gaspayer.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigInteger

data class FundWalletRequest(
    @Schema(
        description = "Wallet address to fund",
        example = "0x742b35Cc6834C0532Fee23f35E4cdb41c176fBc2",
        pattern = "^0x[a-fA-F0-9]{40}$"
    )
    @field:NotBlank(message = "Wallet address is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{40}$",
        message = "Invalid wallet address format"
    )
    val walletAddress: String,

    @Schema(
        description = "Total amount in wei needed by the wallet",
        example = "1000000000000000000"
    )
    @field:Positive(message = "Amount must be positive")
    val totalAmountNeededWei: BigInteger
)