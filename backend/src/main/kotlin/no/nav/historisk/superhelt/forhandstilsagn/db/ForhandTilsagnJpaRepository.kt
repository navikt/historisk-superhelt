package no.nav.historisk.superhelt.forhandstilsagn.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface ForhandTilsagnJpaRepository : JpaRepository<ForhandTilsagnJpaEntity, Long> {}
