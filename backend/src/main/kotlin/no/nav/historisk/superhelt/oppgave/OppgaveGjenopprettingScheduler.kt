package no.nav.historisk.superhelt.oppgave

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OppgaveGjenopprettingScheduler(
    private val oppgaveGjenopprettingService: OppgaveGjenopprettingService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 6 * * *")
    @SchedulerLock(
        name = "OppgaveGjenopprettingScheduler",
        lockAtLeastFor = "PT1M",
        lockAtMostFor = "PT30M",
    )
    fun gjenopprettOppgaver() {
        logger.info("Starter gjenopprettingsjobb for manglende oppgaver")
        oppgaveGjenopprettingService.gjenopprettManglendeOppgaver()
    }
}
