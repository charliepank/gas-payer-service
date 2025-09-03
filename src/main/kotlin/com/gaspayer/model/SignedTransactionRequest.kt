package com.gaspayer.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignedTransactionRequest(
    @Schema(
        description = "User's wallet address that will receive gas transfer if needed",
        example = "0x742b35Cc6834C0532Fee23f35E4cdb41c176fBc2",
        pattern = "^0x[a-fA-F0-9]{40}$"
    )
    @field:NotBlank(message = "User wallet address is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]{40}$",
        message = "Invalid wallet address format"
    )
    val userWalletAddress: String,

    @Schema(
        description = """
            Hex-encoded signed transaction ready for blockchain submission.
            
            **Transaction Format Requirements:**
            
            1. **Hex-encoded signed transactions**: Must be valid hex strings that can be decoded by Web3j's TransactionDecoder.decode()
            
            2. **Supported transaction types**:
               - Legacy transactions: Must have gasPrice field
               - EIP-1559 transactions: Must have maxFeePerGas field
            
            3. **Required transaction fields**:
               - to: Target contract or wallet address
               - value: Transaction value in wei (can be zero)
               - data: Transaction payload (contract method calls, parameters, etc.)
               - gasLimit: Maximum gas units the transaction can consume
               - Gas pricing (one of):
                 - gasPrice (for legacy transactions)
                 - maxFeePerGas (for EIP-1559 transactions)
        """,
        example = "0xf86c2a8504a817c80082520894742b35cc6834c0532fee23f35e4cdb41c176fbc2880de0b6b3a764000080820a95a0c8b7b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3a01c8b7b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3b3",
        pattern = "^0x[a-fA-F0-9]+$"
    )
    @field:NotBlank(message = "Signed transaction hex is required")
    @field:Pattern(
        regexp = "^0x[a-fA-F0-9]+$",
        message = "Invalid signed transaction hex format"
    )
    val signedTransactionHex: String,

    @Schema(
        description = "Name of the blockchain operation for gas cost estimation",
        example = "transfer",
        defaultValue = "unknown"
    )
    val operationName: String = "unknown"
)