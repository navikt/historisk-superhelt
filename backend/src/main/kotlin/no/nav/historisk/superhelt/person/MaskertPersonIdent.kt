package no.nav.historisk.superhelt.person

import no.nav.historisk.superhelt.auth.crypto.CachedEncryptor
import no.nav.historisk.superhelt.auth.crypto.SaltedXorEncryptor
import no.nav.person.Fnr

private object FnrEncryptor {
    private val encryptor =
        CachedEncryptor(SaltedXorEncryptor("not-so-secret-really-todo"), cacheDuration = java.time.Duration.ofHours(1))

    fun encrypt(fnr: String): MaskertPersonIdent {
        return encryptor.encrypt(fnr)
    }

    fun decrypt(maskertFnr: String): Fnr {
        return encryptor.decrypt(maskertFnr)
    }
}

typealias MaskertPersonIdent = String

fun  MaskertPersonIdent.toFnr(): Fnr = FnrEncryptor.decrypt(this)
fun Fnr.toMaskertPersonIdent(): MaskertPersonIdent = FnrEncryptor.encrypt(this)