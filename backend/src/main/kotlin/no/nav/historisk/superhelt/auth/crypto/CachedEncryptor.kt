package no.nav.historisk.superhelt.auth.crypto

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Enkel cache for encryptor som lagrer krypterte verdier i minnet for en gitt periode.
 * Brukes for å redusere antall varianter av samme plaintext som genereres.
 */

class CachedEncryptor(
    private val delegate: Encryptor,
    private val cacheDuration: Duration = Duration.ofHours(1),
    private val clock: Clock = Clock.systemUTC()
) : Encryptor{

    private data class CacheEntry(
        val encrypted: String,
        val expiresAt: Instant
    )

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun encrypt(plaintext: String): String {
        val now = clock.instant()

        // Fjern utløpte entries
        cache.entries.removeIf { it.value.expiresAt.isBefore(now) }

        // Sjekk cache
        cache[plaintext]?.let { return it.encrypted }

        // Encrypt and cache
        val encrypted = delegate.encrypt(plaintext)
        cache[plaintext] = CacheEntry(encrypted, now.plus(cacheDuration))

        return encrypted
    }

    override fun decrypt(ciphertext: String): String {
        return delegate.decrypt(ciphertext)
    }
}
