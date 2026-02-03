package no.nav.historisk.superhelt.infrastruktur.mdc

import org.slf4j.MDC

object MdcHelper {
    const val CALL_ID_HEADER = "Nav-Call-Id"
    const val USER_MDC = "user"

    var callId: String?
        get() = MDC.get(CALL_ID_HEADER)
        set(value) = MDC.put(CALL_ID_HEADER, value)

    var userIdent: String?
        get() = MDC.get(USER_MDC)
        set(value) = MDC.put(USER_MDC, value)

    fun clear() {
        MDC.remove(CALL_ID_HEADER)
        MDC.remove(USER_MDC)
    }
}