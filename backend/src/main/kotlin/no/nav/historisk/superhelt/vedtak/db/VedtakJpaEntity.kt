package no.nav.historisk.superhelt.vedtak.db

import jakarta.persistence.*
import no.nav.common.types.Aar
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Belop
import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.NavUser
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.hibernate.Hibernate
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "vedtak")
class VedtakJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vedtak_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sak_id", nullable = false)
    val sak: SakJpaEntity,

    /** Skiller mellom ulike behandlinger på samme sak. Økes med 1 for hver behandling */
    val behandlingsTeller: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "stonads_type")
    val type: StonadsType,

    val fnr: FolkeregisterIdent,
    val beskrivelse: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "vedtaks_resultat")
    val resultat: VedtaksResultat,
    val begrunnelse: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "utbetalings_type")
    val utbetalingsType: UtbetalingsType,
    val belop: Int?,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "navIdent", column = Column(name = "saksbehandler_nav_ident")),
        AttributeOverride(name = "navn", column = Column(name = "saksbehandler_navn"))
    )
    var saksbehandler: NavUser,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "navIdent", column = Column(name = "attestant_nav_ident")),
        AttributeOverride(name = "navn", column = Column(name = "attestant_navn"))
    )
    var attestant: NavUser,

    val soknadsDato: LocalDate,
    var tildelingsAar: Int?,
    val vedtaksTidspunkt: Instant,

    ) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as VedtakJpaEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    internal fun toDomain(): Vedtak {

        val saksnummer = this.sak.saksnummer
        return Vedtak(
            saksnummer = saksnummer,
            behandlingsnummer = Behandlingsnummer(saksnummer, this.behandlingsTeller),
            stonadstype = this.type,
            fnr = this.fnr,
            beskrivelse = this.beskrivelse,
            begrunnelse = this.begrunnelse,
            resultat = this.resultat,
            saksbehandler = this.saksbehandler,
            attestant = this.attestant,
            soknadsDato = this.soknadsDato,
            tildelingsAar = this.tildelingsAar?.let { Aar(it) },
            vedtaksTidspunkt = vedtaksTidspunkt,
            utbetalingsType = this.utbetalingsType,
            belop = this.belop?.let { Belop(it) },
        )
    }
}
