package no.nav.historisk.superhelt.infrastruktur.crypto

interface Encryptor {
    fun encrypt(plaintext: String): String
    fun decrypt(ciphertext: String): String
}