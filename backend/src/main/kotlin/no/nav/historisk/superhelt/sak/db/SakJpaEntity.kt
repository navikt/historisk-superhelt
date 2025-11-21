package no.nav.historisk.superhelt.sak.db

import jakarta.persistence.*
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Fnr
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.db.ForhandTilsagnJpaEntity
import no.nav.historisk.superhelt.utbetaling.db.UtbetalingJpaEntity
import org.hibernate.Hibernate
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "sak")
class SakJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    var type: StonadsType,

    /** Skiller mellom ulike behandlinger på samme sak. Økes med 1 for hver behandling */
    var behandlingsTeller: Int = 1,

    var fnr: Fnr,

    var tittel: String? = null,

    @Enumerated(EnumType.STRING)
    var status: SakStatus = SakStatus.UNDER_BEHANDLING,

    @Enumerated(EnumType.STRING)
    var vedtak: VedtakType? = null,

    var begrunnelse: String? = null,

    var saksbehandler: NavIdent,
    var attestant: NavIdent? = null,

    var opprettet: Instant = Instant.now(),

    var soknadsDato: LocalDate? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var utbetaling: UtbetalingJpaEntity? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var forhandstilsagn: ForhandTilsagnJpaEntity? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as SakJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    fun setOrUpdateUtbetaling(belop: Int) {
        val entity = this.utbetaling ?: UtbetalingJpaEntity(sak = this, belop = 0)
        entity.belop = belop
        this.utbetaling = entity
    }

    fun setOrUpdateForhandsTilsagn(belop: Int) {
        val entity = this.forhandstilsagn ?: ForhandTilsagnJpaEntity(sak = this, belop = 0)
        entity.belop = belop
        this.forhandstilsagn = entity
    }

    val saksnummer: Saksnummer
        get() = id?.let { Saksnummer(it) }
            ?: throw IllegalStateException(
                "SakJpaEntity id kan ikke være null ved henting av saksnummer"
            )
    val behandlingsnummer: Behandlingsnummer
        get() = Behandlingsnummer(saksnummer.value, behandlingsTeller)

    internal fun toDomain(): Sak {

        return Sak(
            saksnummer = saksnummer,
            behandlingsnummer = behandlingsnummer,
            type = this.type,
            fnr = this.fnr,
            tittel = this.tittel,
            begrunnelse = this.begrunnelse,
            status = this.status,
            vedtak = this.vedtak,
            saksbehandler = this.saksbehandler,
            attestant = this.attestant,
            opprettetDato = this.opprettet,
            soknadsDato = this.soknadsDato,
            utbetaling = this.utbetaling?.toDomain(),
            forhandstilsagn = this.forhandstilsagn?.toDomain(),
        )
    }
}
