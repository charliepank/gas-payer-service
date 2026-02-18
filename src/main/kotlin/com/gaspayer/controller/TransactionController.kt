package com.gaspayer.controller

import com.gaspayer.model.SignedTransactionRequest
import com.gaspayer.model.FundWalletRequest
import com.gaspayer.service.GasPayerService
import com.utility.chainservice.models.TransactionResult
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class TransactionController(
    private val gasPayerService: GasPayerService
) {
    
    private val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @PostMapping("/signed-transaction")
    suspend fun processSignedTransaction(
        @Valid @RequestBody request: SignedTransactionRequest
    ): ResponseEntity<TransactionResult> {
        
        logger.info("Processing signed transaction for wallet: ${request.userWalletAddress}")
        
        return try {
            val result = gasPayerService.processSignedTransaction(
                userWalletAddress = request.userWalletAddress,
                signedTransactionHex = request.signedTransactionHex,
                operationName = request.operationName
            )
            
            if (result.success) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.badRequest().body(result)
            }
            
        } catch (e: Exception) {
            logger.error("Unexpected error in transaction controller for wallet: ${request.userWalletAddress}, operation: ${request.operationName}", e)
            ResponseEntity.internalServerError().body(
                TransactionResult(
                    success = false,
                    transactionHash = null,
                    error = "Internal server error processing transaction for operation '${request.operationName}': ${e.message ?: "Unknown error occurred"}"
                )
            )
        }
    }

    @PostMapping("/fund-wallet")
    suspend fun fundWallet(
        @Valid @RequestBody request: FundWalletRequest
    ): ResponseEntity<TransactionResult> {
        
        logger.info("Processing fund wallet request for wallet: ${request.walletAddress}, amount: ${request.totalAmountNeededWei}")
        
        return try {
            val result = gasPayerService.conditionalFunding(
                walletAddress = request.walletAddress,
                totalAmountNeededWei = request.totalAmountNeededWei
            )
            
            if (result.success) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.badRequest().body(result)
            }
            
        } catch (e: Exception) {
            logger.error("Unexpected error in fund wallet controller for wallet: ${request.walletAddress}, amount: ${request.totalAmountNeededWei}", e)
            ResponseEntity.internalServerError().body(
                TransactionResult(
                    success = false,
                    transactionHash = null,
                    error = "Internal server error processing wallet funding for ${request.walletAddress}: ${e.message ?: "Unknown error occurred"}"
                )
            )
        }
    }
}