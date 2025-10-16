package no.nav.historisk.superhelt.person

import no.nav.historisk.superhelt.auth.crypto.XorWithSaltEncryption
import no.nav.person.Fnr
import org.springframework.stereotype.Service

@Service
class MaskertPersonService {

        private val crypto= XorWithSaltEncryption("todo-fiks-hemmelighet")


    fun maskerFnr(fnr: Fnr): MaskertPersonIdent {
        return crypto.encrypt(fnr)
    }

    fun decodeMaskertFnr(maskertPersonident: MaskertPersonIdent): Fnr {
        return crypto.decrypt(maskertPersonident)
    }
}

typealias MaskertPersonIdent = String