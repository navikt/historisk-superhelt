package no.nav.historisk.superhelt.klage.db

import jakarta.persistence.*
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
    @Column(name = "klage_id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    @Column(name = "hjemmel_id", nullable = false, length = 100)
    var hjemmelId: String,

    @Column(name = "dato_klage_mottatt", nullable = false)
    var datoKlageMottatt: LocalDate,

    @Column(name = "kommentar", columnDefinition = "TEXT")
    var kommentar: String? = null,

    @Column(name = "forrige_behandlende_enhet", nullable = false, length = 10)
    var forrigeBehandlendeEnhet: String,

    @Column(name = "sendt_tidspunkt", nullable = false)
    var sendtTidspunkt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: KlageStatus = KlageStatus.SENDT,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as KlageJpaEntity
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    internal fun toDomain(): Klage = Klage(
        id = id,
        saksnummer = sak.saksnummer,
        hjemmelId = hjemmelId,
        datoKlageMottatt = datoKlageMottatt,
        kommentar = kommentar,
        forrigeBehandlendeEnhet = forrigeBehandlendeEnhet,
        sendtTidspunkt = sendtTidspunkt,
        status = status,
    )
}
