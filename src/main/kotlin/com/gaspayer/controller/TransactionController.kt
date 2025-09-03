package com.gaspayer.controller

import com.gaspayer.model.SignedTransactionRequest
import com.gaspayer.service.GasPayerService
import com.utility.chainservice.models.TransactionResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transaction Processing", description = "API for processing signed transactions with gas management")
class TransactionController(
    private val gasPayerService: GasPayerService
) {
    
    private val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @PostMapping("/signed-transaction")
    @Operation(
        summary = "Process signed transaction with gas management",
        description = """
            Processes a signed transaction by ensuring the wallet has sufficient gas and 
            forwarding it to the blockchain. If the wallet lacks gas, it will be funded 
            automatically before the transaction is submitted.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Transaction processed successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request or transaction validation failed"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during transaction processing"
            )
        ]
    )
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
            logger.error("Error processing signed transaction", e)
            ResponseEntity.internalServerError().body(
                TransactionResult(
                    success = false,
                    transactionHash = null,
                    error = "Internal server error: ${e.message}"
                )
            )
        }
    }
}