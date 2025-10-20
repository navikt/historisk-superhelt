package no.nav.historisk.superhelt.infrastruktur.mdc

import org.slf4j.MDC

object MdcHelper {
    const val CALL_ID_HEADER = "Nav-Call-Id"

    var callId: String?
        get() = MDC.get(CALL_ID_HEADER)
        set(value) = MDC.put(CALL_ID_HEADER, value)

    fun clear() {
        MDC.clear()
    }
}