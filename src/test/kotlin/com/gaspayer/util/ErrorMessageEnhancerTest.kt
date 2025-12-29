package com.gaspayer.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigInteger

class ErrorMessageEnhancerTest {

    @Test
    fun `should enhance gas transfer failure message`() {
        val originalError = "Failed to transfer gas to user: insufficient funds for gas"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "contract_creation")
        
        assertTrue(result.contains("Gas transfer failed for operation 'contract_creation'"))
        assertTrue(result.contains("insufficient funds for gas"))
    }

    @Test
    fun `should enhance gas price too high message`() {
        val originalError = "Gas price too high: provided 50000000000, maximum allowed 30000000000 (current network: 25000000000)"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "token_transfer")

        assertTrue(result.contains("Gas price exceeds limit for operation 'token_transfer'"))
        assertTrue(result.contains(originalError))
    }

    @Test
    fun `should enhance transaction cost too high message`() {
        val originalError = "Transaction cost too high: 1000000000000000000 wei, maximum allowed 500000000000000000 wei"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "escrow_funding")

        assertTrue(result.contains("Transaction cost exceeds limit for operation 'escrow_funding'"))
        assertTrue(result.contains(originalError))
    }

    @Test
    fun `should enhance gas limit exceeded message`() {
        val originalError = "Gas limit exceeds expected for operation 'contract_creation': provided 2000000, maximum allowed 1000000 (includes 20% buffer)"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError)

        assertTrue(result.contains("Gas limit exceeded"))
        assertTrue(result.contains(originalError))
    }

    @Test
    fun `should enhance insufficient funds message`() {
        val originalError = "insufficient funds for gas * price + value"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "token_transfer")
        
        assertTrue(result.contains("Insufficient funds for operation 'token_transfer'"))
        assertTrue(result.contains("gas payer wallet does not have enough ETH"))
    }

    @Test
    fun `should enhance balance update timeout message`() {
        val originalError = "Balance update timeout after gas transfer"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "contract_call")
        
        assertTrue(result.contains("Wallet balance update timeout for operation 'contract_call'"))
        assertTrue(result.contains("transaction may still be processing"))
    }

    @Test
    fun `should enhance authentication error message`() {
        val originalError = "Client wallet credentials required - no wallet configured for this API key"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError)
        
        assertTrue(result.contains("Authentication error"))
        assertTrue(result.contains("API key is properly configured"))
    }

    @Test
    fun `should create detailed transaction error with context`() {
        val originalError = "Gas price too high: provided 50000000000, maximum allowed 30000000000 (current network: 25000000000)"
        val userWalletAddress = "0x1234567890abcdef1234567890abcdef12345678"
        val context = mapOf(
            "gasPrice" to BigInteger("50000000000"),
            "gasLimit" to BigInteger("100000")
        )

        val result = ErrorMessageEnhancer.createDetailedTransactionError(
            originalError = originalError,
            operationName = "token_transfer",
            userWalletAddress = userWalletAddress,
            additionalContext = context
        )

        assertTrue(result.contains("Gas price exceeds limit for operation 'token_transfer'"))
        assertTrue(result.contains("wallet: 0x12345678...5678")) // truncated wallet address
        assertTrue(result.contains("gasPrice: 50000000000")) // context value
        assertTrue(result.contains("gasLimit: 100000")) // context value
    }

    @Test
    fun `should handle network errors appropriately`() {
        val originalError = "execution reverted: ERC20: transfer amount exceeds balance"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "token_transfer")
        
        assertTrue(result.contains("Transaction reverted for operation 'token_transfer'"))
        assertTrue(result.contains("blockchain rejected the transaction"))
    }

    @Test
    fun `should handle nonce errors`() {
        val originalError = "nonce too low"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "contract_creation")
        
        assertTrue(result.contains("Transaction nonce error for operation 'contract_creation'"))
        assertTrue(result.contains("Nonce conflict detected"))
    }

    @Test
    fun `should handle replacement transaction underpriced`() {
        val originalError = "replacement transaction underpriced"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError)
        
        assertTrue(result.contains("Gas price too low"))
        assertTrue(result.contains("requires higher gas price"))
    }

    @Test
    fun `should handle empty or null error messages`() {
        val result1 = ErrorMessageEnhancer.enhanceErrorMessage("", "test_operation")
        assertTrue(result1.contains("Unknown transaction error for operation 'test_operation'"))
        
        val result2 = ErrorMessageEnhancer.enhanceErrorMessage("   ", "test_operation")
        assertTrue(result2.contains("Unknown transaction error for operation 'test_operation'"))
    }

    @Test
    fun `should handle generic errors with operation context`() {
        val originalError = "Some unexpected blockchain error"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError, "escrow_creation")
        
        assertEquals("Transaction failed for operation 'escrow_creation': Some unexpected blockchain error", result)
    }

    @Test
    fun `should work without operation name`() {
        val originalError = "Gas price too high: provided 50000000000, maximum allowed 30000000000 (current network: 25000000000)"
        val result = ErrorMessageEnhancer.enhanceErrorMessage(originalError)
        
        assertTrue(result.contains("Gas price too high:")) // no operation specified
        assertFalse(result.contains("for operation"))
    }
}