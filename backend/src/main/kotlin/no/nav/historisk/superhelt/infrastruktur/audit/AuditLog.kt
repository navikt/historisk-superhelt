package no.nav.historisk.superhelt.infrastruktur.audit

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.mdc.MdcHelper
import org.slf4j.LoggerFactory
import java.time.Instant

/*
Dokumentasjon fra: https://github.com/navikt/naudit

    The log string needs to follow this format:
    CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
    where Extension holds key-value pairs, some of which are standard, and others that may be specific to your context.

    Version: Version, set to 0
    Device Vendor: Application name
    Device Product: Name of the log that originated the event
    Device Version: Version of the log format (start with 1.0)
    Device Event Class ID: Type of the event (CRUD, audit:create, audit:access, audit:update, audit:delete)
    Name: Description
    Severity: Severity of the event, INFO or WARN
    Standard parts of the extension: end=<timestamp> suid=<NAV-ident> duid=<Fnr/Aktør-id> sproc=<call-id> flexString1Label=Decision flexString1=<Permit/Deny>
 */
object AuditLog {
    private val applicationName = "superhelt"
    private val logger = LoggerFactory.getLogger("audit")

    fun log(
        fnr: FolkeregisterIdent,
        message: String,
        decision: Decision = Decision.PERMIT,
        customIdentifierAndValue: Pair<String, String>? = null,
    ) {

        val deviceProduct = "sporingslogg"
        val deviceVersion = "1.0"
        val eventType = "audit:access"
        val name = "Auditlogg"

        val callId = MdcHelper.callId
        val navIdent = getAuthenticatedUser().navIdent

        val timestamp = Instant.now().toEpochMilli()

        val cefString = "CEF:0|$applicationName|$deviceProduct|$deviceVersion|$eventType|${name}|INFO|"
        val extentionList = mutableListOf(
            "end=$timestamp",
            "suid=${navIdent}",
            "duid=${fnr.value}",
            "sproc=${callId}",
            "msg=${message}",
            "flexString1Label=Decision",
            "flexString1=${decision.key}",
        )
        customIdentifierAndValue?.let {
            extentionList.add("flexString2Label=${it.first} flexString2=${it.second}")
        }


        logger.info("${cefString}${extentionList.joinToString(" ")}")
    }

    enum class Decision(
        val key: String,
    ) {
        PERMIT("Permit"),
        DENY("Deny"),
    }
}
