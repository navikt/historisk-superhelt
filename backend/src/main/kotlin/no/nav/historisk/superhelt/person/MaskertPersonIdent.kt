package no.nav.historisk.superhelt.person

import no.nav.historisk.superhelt.auth.crypto.CachedEncryptor
import no.nav.historisk.superhelt.auth.crypto.SaltedXorEncryptor
import no.nav.person.Fnr
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private object FnrEncryptor {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val encryptor =
        CachedEncryptor(SaltedXorEncryptor("not-so-secret-really-todo"), cacheDuration = java.time.Duration.ofHours(1))

    fun encrypt(fnr: String): MaskertPersonIdent {
        val encrypt = encryptor.encrypt(fnr)
        return MaskertPersonIdent(encrypt)
    }

    fun decrypt(maskertFnr: String): Fnr? {
        try {
            val decrypted = encryptor.decrypt(maskertFnr)
            val fnr = Fnr(decrypted)
            if (fnr.isValid()) {
                return fnr
            } else {
                logger.info("Dekryptert fnr er ikke gyldig: $decrypted")
            }
        } catch (e: IllegalArgumentException) {
            logger.info("Feil ved dekryptering av fnr", e)
        }
        return null
    }
}

@JvmInline
value class MaskertPersonIdent(val value: String) {
    fun toFnr() = FnrEncryptor.decrypt(this.value)
}

fun Fnr.toMaskertPersonIdent(): MaskertPersonIdent = FnrEncryptor.encrypt(this.value)