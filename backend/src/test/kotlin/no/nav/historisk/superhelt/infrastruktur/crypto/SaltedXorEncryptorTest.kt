package no.nav.historisk.superhelt.infrastruktur.crypto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SaltedXorEncryptorTest {

    private val secretKey = "correct horse battery staple"
    private val encryptor = SaltedXorEncryptor(secretKey)

    @Test
    fun `encrypt og decrypt skal returnere original tekst`() {
        // Arrange
        val plaintext = "12345678901"

        // Act
        val encrypted = encryptor.encrypt(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isEqualTo(plaintext)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "12345678901",
        "98765432109",
        "11111111111",
        "00000000000"
    ])
    fun `encrypt og decrypt skal fungere for ulike fødselsnummer`(fnr: String) {
        // Act
        val encrypted = encryptor.encrypt(fnr)
        val decrypted = encryptor.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isEqualTo(fnr)
    }

    @Test
    fun `encrypt skal produsere ulike ciphertekster for samme plaintext`() {
        // Arrange
        val plaintext = "12345678901"

        // Act
        val encrypted1 = encryptor.encrypt(plaintext)
        val encrypted2 = encryptor.encrypt(plaintext)

        // Assert
        Assertions.assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `encrypt skal produsere base64 url-safe streng`() {
        // Arrange
        val plaintext = "12345678901"

        // Act
        val encrypted = encryptor.encrypt(plaintext)

        // Assert
        Assertions.assertThat(encrypted).matches("^[A-Za-z0-9_-]+$")
    }

    @Test
    fun `decrypt skal kaste exception for ugyldig ciphertext`() {
        // Arrange
        val invalidCiphertext = "abc"

        // Act & Assert
        Assertions.assertThatThrownBy { encryptor.decrypt(invalidCiphertext) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `decrypt skal kaste exception for tom ciphertext`() {
        // Arrange
        val emptyCiphertext = ""

        // Act & Assert
        Assertions.assertThatThrownBy { encryptor.decrypt(emptyCiphertext) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `constructor skal kaste exception for tom key`() {
        // Act & Assert
        Assertions.assertThatThrownBy { SaltedXorEncryptor("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Key cannot be empty")
    }

    @Test
    fun `ulike keys skal produsere ulike ciphertekster`() {
        // Arrange
        val plaintext = "12345678901"
        val encryptor1 = SaltedXorEncryptor("key1")
        val encryptor2 = SaltedXorEncryptor("key2")

        // Act
        val encrypted1 = encryptor1.encrypt(plaintext)
        val encrypted2 = encryptor2.encrypt(plaintext)

        // Assert
        Assertions.assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `decrypt med feil key skal ikke returnere original tekst`() {
        // Arrange
        val plaintext = "12345678901"
        val encryptor1 = SaltedXorEncryptor("correctKey")
        val encryptor2 = SaltedXorEncryptor("wrongKey")

        // Act
        val encrypted = encryptor1.encrypt(plaintext)
        val decrypted = encryptor2.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isNotEqualTo(plaintext)
    }

    @Test
    fun `encrypt skal håndtere spesialtegn`() {
        // Arrange
        val plaintext = "æøå!@#$%^&*()"

        // Act
        val encrypted = encryptor.encrypt(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt skal håndtere tom streng`() {
        // Arrange
        val plaintext = ""

        // Act
        val encrypted = encryptor.encrypt(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt skal håndtere lang tekst`() {
        // Arrange
        val plaintext = "a".repeat(1000)

        // Act
        val encrypted = encryptor.encrypt(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        // Assert
        Assertions.assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypted tekst skal være lengre enn original på grunn av salt`() {
        // Arrange
        val plaintext = "12345678901"

        // Act
        val encrypted = encryptor.encrypt(plaintext)

        // Assert
        // Base64 encoding øker størrelsen, pluss salt på 4 bytes
        Assertions.assertThat(encrypted.length).isGreaterThan(plaintext.length)
    }

    @Test
    fun `samme plaintext kryptert flere ganger skal alle dekrypteres korrekt`() {
        // Arrange
        val plaintext = "12345678901"
        val iterations = 10

        // Act
        val encryptedList = (1..iterations).map { encryptor.encrypt(plaintext) }
        val decryptedList = encryptedList.map { encryptor.decrypt(it) }

        // Assert
        Assertions.assertThat(decryptedList).hasSize(iterations)
        Assertions.assertThat(decryptedList).allMatch { it == plaintext }
        Assertions.assertThat(encryptedList.distinct()).hasSize(iterations) // Alle skal være unike
    }
}