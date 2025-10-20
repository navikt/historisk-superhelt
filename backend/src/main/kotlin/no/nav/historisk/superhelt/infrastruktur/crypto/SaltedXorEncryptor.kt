package no.nav.historisk.superhelt.infrastruktur.crypto

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/**
 * Enkel kryptering med XOR og salt.
 * Ikke egnet for sensitive data i produksjon.
 * Kun for å hindre enkel lesing av data i f.eks. URL.
 */

@OptIn(ExperimentalEncodingApi::class)
class SaltedXorEncryptor(private val key: String): Encryptor {

    init {
        require(key.isNotEmpty()) { "Key cannot be empty" }
    }

    private val saltSize = 4 // bytes

    override fun encrypt(plaintext: String): String {
        val salt = ByteArray(saltSize)
        Random.Default.nextBytes(salt)

        val keyBytes = key.toByteArray()
        val plainBytes = plaintext.toByteArray()

        // XOR med key og salt
        val encrypted = ByteArray(plainBytes.size)
        for (i in plainBytes.indices) {
            val keyByte = keyBytes[i % keyBytes.size]
            val saltByte = salt[i % salt.size]
            encrypted[i] = (plainBytes[i].toInt() xor keyByte.toInt() xor saltByte.toInt()).toByte()
        }

        // Concatenate salt + encrypted (mer idiomatisk Kotlin)
        val result = salt + encrypted
        return Base64.UrlSafe.encode(result)
    }

    override fun decrypt(ciphertext: String): String {
        val data = Base64.UrlSafe.decode(ciphertext)
        require(data.size >= saltSize) { "Invalid ciphertext size ${data.size} bytes" }

        // Ekstraher salt og encrypted (mer idiomatisk Kotlin)
        val salt = data.copyOfRange(0, saltSize)
        val encrypted = data.copyOfRange(saltSize, data.size)

        val keyBytes = key.toByteArray()

        // XOR tilbake
        val decrypted = ByteArray(encrypted.size)
        for (i in encrypted.indices) {
            val keyByte = keyBytes[i % keyBytes.size]
            val saltByte = salt[i % salt.size]
            decrypted[i] = (encrypted[i].toInt() xor keyByte.toInt() xor saltByte.toInt()).toByte()
        }

        return String(decrypted)
    }
}