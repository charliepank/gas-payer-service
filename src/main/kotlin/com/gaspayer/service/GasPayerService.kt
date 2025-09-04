package com.gaspayer.service

import com.utility.chainservice.BlockchainRelayService
import com.utility.chainservice.models.TransactionResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.web3j.crypto.Credentials

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
            
            blockchainRelayService.processTransactionWithGasTransfer(
                userWalletAddress = userWalletAddress,
                signedTransactionHex = signedTransactionHex,
                operationName = operationName,
                clientCredentials = clientCredentials
            )
        } catch (e: Exception) {
            logger.error("Error processing signed transaction", e)
            TransactionResult(
                success = false,
                transactionHash = null,
                error = "Failed to process transaction: ${e.message}"
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