package com.gaspayer.service

import com.utility.chainservice.BlockchainRelayService
import com.utility.chainservice.models.TransactionResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
            blockchainRelayService.processTransactionWithGasTransfer(
                userWalletAddress = userWalletAddress,
                signedTransactionHex = signedTransactionHex,
                operationName = operationName
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
}