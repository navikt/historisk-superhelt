package no.nav.historisk.superhelt.person

import no.nav.historisk.superhelt.auth.crypto.CachedEncryptor
import no.nav.historisk.superhelt.auth.crypto.SaltedXorEncryptor
import no.nav.historisk.superhelt.auth.exception.IkkeFunnetException
import no.nav.person.Fnr
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private object FnrEncryptor {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val propertyName = "APP_FNR_ENCRYPTOR_SECRET"
    private val secret: String =
        System.getProperty(propertyName)
            ?: System.getenv(propertyName)
            ?: RandomStringUtils.secure().nextPrint(8)

    private val encryptor =
        CachedEncryptor(SaltedXorEncryptor(secret), cacheDuration = java.time.Duration.ofHours(1))

    fun encrypt(fnr: String): MaskertPersonIdent {
        val encrypt = encryptor.encrypt(fnr)
        return MaskertPersonIdent(encrypt)
    }

    fun decrypt(maskertFnr: String): Fnr {
        try {
            val decrypted = encryptor.decrypt(maskertFnr)
            val fnr = Fnr(decrypted)
            if (!fnr.isValid()) {
                logger.info("Feil ved validering av fnr {}", fnr)
                throw IkkeFunnetException("Ugyldig personident dekryptert for $maskertFnr")
            }
            return fnr

        } catch (e: IllegalArgumentException) {
            logger.info("Feil ved dekryptering av fnr", e)
            throw IkkeFunnetException("Ugyldig personident $maskertFnr", e)
        }
    }
}

@JvmInline
value class MaskertPersonIdent(val value: String) {
    /**
     * Dekrypterer det maskerte personidentet til et gyldig Fnr-objekt.
     *
     * @throws IkkeFunnetException hvis dekrypteringen mislykkes eller hvis det resulterende Fnr-objektet ikke er gyldig.
     */
    fun toFnr() = FnrEncryptor.decrypt(this.value)
}

fun Fnr.toMaskertPersonIdent(): MaskertPersonIdent = FnrEncryptor.encrypt(this.value)