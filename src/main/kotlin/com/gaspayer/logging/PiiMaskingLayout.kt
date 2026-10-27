package com.gaspayer.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent

/**
 * This service's console layout, with personal data masked in the finished line — message,
 * arguments and stack trace alike, whatever the pattern says. logback-spring.xml puts it over the
 * service's own console pattern, so the format is unchanged.
 *
 * ⚠️ WHY THE WHOLE LINE. Masking at each log call leaves the next one leaking; masking only the
 *    message misses the exception text, where an email in an error message ends up. Emails and
 *    wallets went to Grafana verbatim until 2026-09-24 (thousands of lines a day).
 *
 * The same rule as the webapp and the log shipper, so every log reads the same:
 *   charliepank@gmail.com                       → ch***@gmail.com
 *   0xd36698991ef328275c8f9598a33b2378d7ece183  → 0xd366***e183
 * Transaction hashes (64 hex digits) are not personal and are left alone.
 *
 * Credentials are masked outright: JWTs (session tokens), Bearer/Basic values, and the value of
 * any auth-token / privy-* / session / api-key / password field — contractservice's auth debug
 * logged whole Cookie headers until 2026-09-24. The call sites no longer log them; this is the net.
 */
class PiiMaskingLayout : PatternLayout() {
    override fun doLayout(event: ILoggingEvent): String = PiiMasking.mask(super.doLayout(event))
}

object PiiMasking {
    private val EMAIL = Regex("""([A-Za-z0-9._%+-]{1,2})[A-Za-z0-9._%+-]*@((?:[A-Za-z0-9-]+\.)+[A-Za-z]{2,})""")

    // Exactly 40 hex digits: \b on both sides keeps it out of a 64-digit transaction hash.
    private val ADDRESS = Regex("""\b(0x[0-9a-fA-F]{4})[0-9a-fA-F]{32}([0-9a-fA-F]{4})\b""")

    // Credentials: nothing about them is worth keeping in a log.
    // A JWT, whole or truncated (session tokens: AUTH-TOKEN, privy-token, Bearer values).
    private val JWT = Regex("""eyJ[A-Za-z0-9_-]{10,}(?:\.[A-Za-z0-9_-]+){0,2}""")
    private val BEARER = Regex("""(?i)\b(bearer|basic)\s+[A-Za-z0-9._~+/=-]{8,}""")
    // The value after a credential's name, in a cookie header, a map dump, JSON or a query string.
    private val CREDENTIAL = Regex(
        """(?i)\b(auth-token|privy-token|privy-id-token|privy-refresh-token|session-?token|session|x-api-key|api[_-]?key|password|secret)(["']?\s*[=:]\s*["']?)[^;,\s'"&}]{4,}"""
    )

    fun mask(text: String): String {
        var out = JWT.replace(text, "eyJ***")
        out = BEARER.replace(out, "\$1 ***")
        out = CREDENTIAL.replace(out, "\$1\$2***")
        out = EMAIL.replace(out, "\$1***@\$2")
        return ADDRESS.replace(out, "\$1***\$2")
    }
}
