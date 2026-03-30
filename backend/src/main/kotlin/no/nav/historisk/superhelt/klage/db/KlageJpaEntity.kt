package no.nav.historisk.superhelt.klage.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.klage.Klage
import no.nav.historisk.superhelt.klage.KlageStatus
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import org.hibernate.Hibernate
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "klage")
class KlageJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "klage_id", nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    @Column(name = "hjemmel_id", nullable = false)
    var hjemmelId: String,

    @Column(name = "dato_klage_mottatt", nullable = false)
    var datoKlageMottatt: LocalDate,

    @Column(name = "kommentar")
    var kommentar: String? = null,

    @Column(name = "kabal_behandling_id")
    var kabalBehandlingId: String? = null,

    @Column(name = "opprettet_tidspunkt", nullable = false)
    var opprettetTidspunkt: Instant = Instant.now(),

    @Column(name = "opprettet_av", nullable = false)
    var opprettetAv: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: KlageStatus = KlageStatus.SENDT,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as KlageJpaEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Klage = Klage(
        id = id!!,
        saksnummer = sak.saksnummer,
        hjemmelId = hjemmelId,
        datoKlageMottatt = datoKlageMottatt,
        kommentar = kommentar,
        kabalBehandlingId = kabalBehandlingId,
        opprettetTidspunkt = opprettetTidspunkt,
        opprettetAv = NavIdent(opprettetAv),
        status = status,
    )
}

