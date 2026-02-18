package com.gaspayer.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignedTransactionRequest(
    @field:NotBlank(message = "User wallet address is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{40}$",
        message = "Invalid wallet address format"
    )
    val userWalletAddress: String,

    @field:NotBlank(message = "Signed transaction hex is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]+$",
        message = "Invalid signed transaction hex format"
    )
    val signedTransactionHex: String,

    @field:NotBlank(message = "Operation name is required")
    val operationName: String
)