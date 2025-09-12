package com.gaspayer.util

import org.slf4j.LoggerFactory

/**
 * Utility class for enhancing error messages with detailed gas information,
 * USD amounts, and user-friendly formatting.
 */
object ErrorMessageEnhancer {
    
    private val logger = LoggerFactory.getLogger(ErrorMessageEnhancer::class.java)
    
    
    /**
     * Extracts specific error information from blockchain-relay-utility error messages
     */
    fun enhanceErrorMessage(originalError: String, operationName: String? = null): String {
        val operation = operationName?.let { " for operation '$it'" } ?: ""
        
        return when {
            // Gas transfer failures
            originalError.contains("Failed to transfer gas to user") -> {
                val details = extractDetailsAfterColon(originalError)
                "Gas transfer failed${operation}: $details"
            }
            
            // Gas validation errors - pass through since blockchain-relay-utility already formats amounts
            originalError.contains("Transaction cost too high") -> {
                "Transaction cost exceeds limit${operation}: $originalError"
            }
            
            originalError.contains("Gas limit exceeds expected") || originalError.contains("Gas limit too high") -> {
                "Gas limit exceeded${operation}: $originalError"
            }
            
            originalError.contains("Gas price too high") -> {
                "Gas price exceeds limit${operation}: $originalError"
            }
            
            // Balance and funding issues
            originalError.contains("Balance update timeout") -> {
                "Wallet balance update timeout${operation}: Unable to confirm gas transfer completion. The transaction may still be processing on the blockchain."
            }
            
            originalError.contains("Client wallet credentials required") -> {
                "Authentication error${operation}: No wallet configured for this API key. Please ensure your API key is properly configured with a gas payer wallet."
            }
            
            originalError.contains("Gas Payer Contract not configured") -> {
                "Configuration error${operation}: Gas payer contract address not configured (GAS_PAYER_CONTRACT_ADDRESS missing). Contact support to resolve this issue."
            }
            
            originalError.contains("Gas transfer transaction failed") -> {
                val details = extractDetailsAfterColon(originalError)
                "Gas transfer transaction failed${operation}: $details. This may indicate insufficient funds in the gas payer wallet or blockchain network issues."
            }
            
            // Insufficient funds patterns
            originalError.contains("insufficient funds", ignoreCase = true) -> {
                "Insufficient funds${operation}: The gas payer wallet does not have enough ETH to cover transaction costs. Please contact support to fund the gas payer wallet."
            }
            
            // Network/RPC errors
            originalError.contains("execution reverted", ignoreCase = true) -> {
                "Transaction reverted${operation}: The blockchain rejected the transaction. This may be due to contract logic constraints or invalid parameters."
            }
            
            originalError.contains("nonce too low", ignoreCase = true) -> {
                "Transaction nonce error${operation}: Nonce conflict detected. The transaction may have already been processed or there's a blockchain synchronization issue."
            }
            
            originalError.contains("replacement transaction underpriced", ignoreCase = true) -> {
                "Gas price too low${operation}: Transaction replacement requires higher gas price than the pending transaction."
            }
            
            // Generic enhancement
            else -> {
                if (originalError.isNotBlank()) {
                    "Transaction failed${operation}: $originalError"
                } else {
                    "Unknown transaction error${operation}: An unexpected error occurred during processing."
                }
            }
        }
    }
    
    /**
     * Extracts details after the first colon in an error message
     */
    private fun extractDetailsAfterColon(message: String): String {
        val colonIndex = message.indexOf(": ")
        return if (colonIndex != -1 && colonIndex < message.length - 2) {
            message.substring(colonIndex + 2)
        } else {
            message
        }
    }
    
    /**
     * Creates a detailed error message for transaction failures with context
     */
    fun createDetailedTransactionError(
        originalError: String,
        operationName: String?,
        userWalletAddress: String,
        additionalContext: Map<String, Any>? = null
    ): String {
        val enhancedMessage = enhanceErrorMessage(originalError, operationName)
        val context = mutableListOf<String>()
        
        // Add wallet context
        context.add("wallet: ${userWalletAddress.take(8)}...${userWalletAddress.takeLast(4)}")
        
        // Add additional context if provided
        additionalContext?.forEach { (key, value) ->
            context.add("$key: $value")
        }
        
        return if (context.isNotEmpty()) {
            "$enhancedMessage [${context.joinToString(", ")}]"
        } else {
            enhancedMessage
        }
    }
}