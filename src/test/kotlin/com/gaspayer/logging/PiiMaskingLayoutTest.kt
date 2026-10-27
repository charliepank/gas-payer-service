package com.gaspayer.logging

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Personal data never reaches this service's console in the clear — checked through the real
 * logback-spring.xml, not just the masking function, so a config that stops using the layout fails.
 */
class PiiMaskingLayoutTest {

    private val email = "charliepank@gmail.com"
    private val wallet = "0xd36698991ef328275c8f9598a33b2378d7ece183"
    private val txHash = "0x" + "ab".repeat(32)

    @Test
    fun `emails and wallets are masked, keeping enough to correlate`() {
        assertEquals("ch***@gmail.com", PiiMasking.mask(email))
        assertEquals("a***@b.co", PiiMasking.mask("a@b.co"))
        assertEquals("0xd366***e183", PiiMasking.mask(wallet))
        assertEquals("tx $txHash", PiiMasking.mask("tx $txHash"), "a transaction hash is not personal")
    }

    @Test
    fun `credentials never print - tokens, bearer values, cookie and api-key values`() {
        val jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2YWFkMGY3MWQwZTFlNDM3ZjkxYjQ1MWEifQ.c2lnbmF0dXJlLWJ5dGVz"
        val cookieHeader = "AUTH HEADER - cookie: '_ga=GA1.1.45; privy-session=t; AUTH-TOKEN=$jwt; privy-id-token=$jwt'"
        val masked = PiiMasking.mask(cookieHeader)
        assertFalse(masked.contains("eyJhbGciOiJIUzUxMiJ9"), masked)
        assertTrue(masked.contains("_ga=GA1.1.45"), "non-credential cookies are left readable: $masked")

        assertEquals("Authorization: Bearer ***", PiiMasking.mask("Authorization: Bearer $jwt").replace("eyJ***", "***"))
        assertEquals("X-API-Key: '***'", PiiMasking.mask("X-API-Key: 'api_key_blah_blah'"))
        assertEquals("{apiKey=***, amount=5}", PiiMasking.mask("{apiKey=s3cr3tvalue, amount=5}"))
        assertEquals("token prefix eyJ***...", PiiMasking.mask("token prefix eyJhbGciOiJIUzUxMiJ9...".replace("...", "") + "..."))
        assertEquals("sessionCookie extracted: false", PiiMasking.mask("sessionCookie extracted: false"), "not a value")
    }

    @Test
    fun `the configured console masks the message, its arguments and the stack trace`() {
        val printed = logThroughTheRealConfig { logger ->
            logger.info("User {} bound wallet {}", email, wallet)
            logger.error("lookup failed", IllegalStateException("no verified address for $email"))
        }

        assertFalse(printed.contains(email), printed)
        assertFalse(printed.contains(wallet), printed)
        assertTrue(printed.contains("User ch***@gmail.com bound wallet 0xd366***e183"), printed)
        assertTrue(printed.contains("no verified address for ch***@gmail.com"), printed)
    }

    /** Configures a fresh logback context from logback-spring.xml and returns what reached stdout. */
    private fun logThroughTheRealConfig(block: (Logger) -> Unit): String {
        val out = ByteArrayOutputStream()
        val original = System.out
        System.setOut(PrintStream(out, true, Charsets.UTF_8))
        try {
            // A hand-made context needs its MDC adapter set; Spring does this in the real service.
            val context = LoggerContext().apply { setMDCAdapter(ch.qos.logback.classic.util.LogbackMDCAdapter()) }
            JoranConfigurator().apply { this.context = context }
                .doConfigure(javaClass.classLoader.getResource("logback-spring.xml")!!)
            block(context.getLogger("pii-test"))
            // Logback reports config and append problems as statuses, not exceptions: a broken
            // setup would otherwise just print nothing.
            val problems = context.statusManager.copyOfStatusList.filter { it.level >= ch.qos.logback.core.status.Status.WARN }
            assertTrue(problems.isEmpty(), "logging through logback-spring.xml failed: " + problems.joinToString { it.message + " " + (it.throwable ?: "") })
            context.stop()
        } finally {
            System.setOut(original)
        }
        return out.toString(Charsets.UTF_8)
    }
}
