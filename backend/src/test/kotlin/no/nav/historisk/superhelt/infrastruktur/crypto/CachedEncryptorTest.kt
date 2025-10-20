package no.nav.historisk.superhelt.infrastruktur.crypto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Duration
import java.time.Instant

class CachedEncryptorTest {

    private val secretKey = "correct horse battery staple"
    private val encryptor = SaltedXorEncryptor(secretKey)
    val cachedEncryptor = CachedEncryptor(encryptor, Duration.ofMinutes(5))

    @Test
    fun `encrypt skal returnere samme verdi fra cache når tiden ikke har gått ut`() {
        // Arrange
        val plaintext = "12345678901"

        // Act
        val encrypted1 = cachedEncryptor.encrypt(plaintext)
        val encrypted2 = cachedEncryptor.encrypt(plaintext)
        val encrypted3 = cachedEncryptor.encrypt(plaintext)

        // Assert
        assertThat(encrypted1).isEqualTo(encrypted2)
        assertThat(encrypted1).isEqualTo(encrypted3)
        assertThat(cachedEncryptor.decrypt(encrypted1)).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt skal returnere ny verdi når cache har gått ut`() {
        val plaintext = "12345678901"

        val mockClock = mock<Clock>()
        val startTime = Instant.now()
        val expiredTime = startTime.plus(Duration.ofMinutes(10))

        whenever(mockClock.instant()).thenReturn(startTime, expiredTime)
        val cachedEncryptor = CachedEncryptor(encryptor, Duration.ofMinutes(5), mockClock)

        val encrypted1 = cachedEncryptor.encrypt(plaintext)
        val encrypted2 = cachedEncryptor.encrypt(plaintext)

        assertThat(encrypted1).isNotEqualTo(encrypted2)

        assertThat(cachedEncryptor.decrypt(encrypted1)).isEqualTo(plaintext)
        assertThat(cachedEncryptor.decrypt(encrypted2)).isEqualTo(plaintext)
    }


}
