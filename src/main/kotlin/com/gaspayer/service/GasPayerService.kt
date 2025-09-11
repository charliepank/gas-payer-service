package com.gaspayer.service

import com.gaspayer.util.ErrorMessageEnhancer
import com.utility.chainservice.BlockchainRelayService
import com.utility.chainservice.models.TransactionResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.web3j.crypto.Credentials
import java.math.BigInteger

@Service
class GasPayerService(
    private val blockchainRelayService: BlockchainRelayService
) {
    
    private val logger = LoggerFactory.getLogger(GasPayerService::class.java)

    suspend fun processSignedTransaction(
        userWalletAddress: String,
        signedTransactionHex: String,
        operationName: String
    ): TransactionResult {
        
        logger.info("Processing transaction for wallet: $userWalletAddress, operation: $operationName")
        
        return try {
            // Extract client credentials from request attributes (set by ApiKeyAuthenticationFilter)
            val clientCredentials = getClientCredentials()
            
            val result = blockchainRelayService.processTransactionWithGasTransfer(
                userWalletAddress = userWalletAddress,
                signedTransactionHex = signedTransactionHex,
                operationName = operationName,
                clientCredentials = clientCredentials
            )
            
            // If the transaction failed, enhance the error message
            if (!result.success && !result.error.isNullOrBlank()) {
                val enhancedError = ErrorMessageEnhancer.createDetailedTransactionError(
                    originalError = result.error!!,
                    operationName = operationName,
                    userWalletAddress = userWalletAddress
                )
                
                logger.warn("Transaction failed with enhanced details: $enhancedError")
                
                return result.copy(error = enhancedError)
            }
            
            result
        } catch (e: Exception) {
            logger.error("Error processing signed transaction for wallet: $userWalletAddress, operation: $operationName", e)
            
            val enhancedError = ErrorMessageEnhancer.createDetailedTransactionError(
                originalError = e.message ?: "Unknown error occurred",
                operationName = operationName,
                userWalletAddress = userWalletAddress,
                additionalContext = mapOf("exceptionType" to (e::class.simpleName ?: "Unknown"))
            )
            
            TransactionResult(
                success = false,
                transactionHash = null,
                error = enhancedError
            )
        }
    }

    suspend fun conditionalFunding(
        walletAddress: String,
        totalAmountNeededWei: BigInteger
    ): TransactionResult {
        
        logger.info("Processing conditional funding for wallet: $walletAddress, amount: $totalAmountNeededWei")
        
        return try {
            val result = blockchainRelayService.conditionalFunding(
                walletAddress = walletAddress,
                totalAmountNeededWei = totalAmountNeededWei
            )
            
            // If the funding failed, enhance the error message
            if (!result.success && !result.error.isNullOrBlank()) {
                val enhancedError = ErrorMessageEnhancer.createDetailedTransactionError(
                    originalError = result.error!!,
                    operationName = "wallet_funding",
                    userWalletAddress = walletAddress,
                    additionalContext = mapOf(
                        "requestedAmount" to ErrorMessageEnhancer.formatWeiScientific(totalAmountNeededWei)
                    )
                )
                
                logger.warn("Conditional funding failed with enhanced details: $enhancedError")
                
                return result.copy(error = enhancedError)
            }
            
            result
        } catch (e: Exception) {
            logger.error("Error processing conditional funding for wallet: $walletAddress, amount: $totalAmountNeededWei", e)
            
            val enhancedError = ErrorMessageEnhancer.createDetailedTransactionError(
                originalError = e.message ?: "Unknown error occurred",
                operationName = "wallet_funding",
                userWalletAddress = walletAddress,
                additionalContext = mapOf(
                    "requestedAmount" to ErrorMessageEnhancer.formatWeiScientific(totalAmountNeededWei),
                    "exceptionType" to (e::class.simpleName ?: "Unknown")
                )
            )
            
            TransactionResult(
                success = false,
                transactionHash = null,
                error = enhancedError
            )
        }
    }
    
    private fun getClientCredentials(): Credentials? {
        return try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            requestAttributes?.request?.getAttribute("client.credentials") as? Credentials
        } catch (e: Exception) {
            logger.warn("Could not extract client credentials from request: ${e.message}")
            null
        }
    }
}