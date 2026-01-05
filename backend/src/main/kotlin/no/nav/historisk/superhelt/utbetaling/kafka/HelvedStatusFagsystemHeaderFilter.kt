package no.nav.historisk.superhelt.utbetaling.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.adapter.RecordFilterStrategy
import org.springframework.stereotype.Component

@Component
class HelvedStatusFagsystemHeaderFilter : RecordFilterStrategy<String, String> {
    /**
     * Filters out messages that do not have the "fagsystem" header set to "HISTORISK".
     * @return true if the message should be discarded
     */
    override fun filter(consumerRecord: ConsumerRecord<String?, String?>): Boolean {
        val fagsystemheader = consumerRecord.headers().lastHeader("fagsystem")
        val discard = fagsystemheader == null || "HISTORISK" != String(fagsystemheader.value())
        return discard
    }
}