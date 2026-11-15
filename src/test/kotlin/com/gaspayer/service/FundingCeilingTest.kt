package com.gaspayer.service

import com.utility.chainservice.BlockchainProperties
import com.utility.chainservice.BlockchainRelayService
import com.utility.chainservice.GasProperties
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.math.BigInteger

/**
 * Funding is held to the ceiling a signed transaction's cost is held to. It had none: on
 * 2026-11-15 a caller asked for 0.0002 ETH, three times that ceiling, and was sent it.
 */
@DisplayName("Wallet funding is capped at the maximum gas cost")
class FundingCeilingTest {

    private val relay: BlockchainRelayService = mock(BlockchainRelayService::class.java)
    private val service = GasPayerService(
        relay,
        BlockchainProperties(gas = GasProperties(maxGasCostWei = 73_000_000_000_000))
    )
    private val wallet = "0xC44a241AbfCfb942A77d01dc24478526AE3a2cBe"

    @Test
    fun `more than the maximum is refused without touching the chain`() {
        val result = runBlocking { service.conditionalFunding(wallet, BigInteger("200000000000000")) }

        assertFalse(result.success)
        assertTrue(result.error!!.contains("maximum allowed 73000000000000 wei"), result.error)
        verifyNoInteractions(relay)
    }
}
