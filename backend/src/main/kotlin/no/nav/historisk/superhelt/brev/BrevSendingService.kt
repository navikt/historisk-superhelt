package no.nav.historisk.superhelt.brev

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BrevSendingService(
    private val brevService: BrevService,
    private val brevRepository: BrevRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendBrev(sak: Sak, brev: Brev) = sendBrev(sak, brev.uuid)

    fun sendBrev(sak: Sak, brevId: BrevId) {
        val brev = brevService.hentBrev(sak, brevId)
        try {
            if (brev.status == BrevStatus.SENDT) {
                log.info("Brev {} er allerede sendt", brevId)
                return
            }
            // Idempotente transaksjonelle operasjoner som kan kjøres flere granger
            brevService.klargjorForSending(sak, brevId)
            brevService.arkiver(sak, brevId)
            brevService.distribuer(sak, brevId)

        } catch (e: Exception) {
            log.error("Feil ved sending av brev ", kv("brevId", brevId), kv("sak", sak.saksnummer), e)
            brevRepository.oppdaterStatus(brevId = brevId, feil = true)
        }

    }
}
