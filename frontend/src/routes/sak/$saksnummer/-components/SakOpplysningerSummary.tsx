import type {Sak} from "@generated";
import {FormSummary} from "@navikt/ds-react";
import {isoTilLokal} from "~/common/dato.utils";
import type {UtbetalingsType} from "~/common/sak/sak.types";
import {useSakStatusNavn} from "~/common/sak/useSakStatusNavn";
import {useSakVedtakNavn} from "~/common/sak/useSakVedtakNavn";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";

interface SakSummaryProps {
    sak: Sak;
}

export default function SakOpplysningerSummary({ sak }: SakSummaryProps) {
    const getStonadsTypeNavn = useStonadsTypeNavn();
    const getSakStatusNavn = useSakStatusNavn();
    const getSakVedtakNavn = useSakVedtakNavn();

    const utbetalingsTypeText = (utbetalingsType: UtbetalingsType) => {
        switch (utbetalingsType) {
            case "BRUKER":
                return "Utbetaling direkte til bruker";
            case "FORHANDSTILSAGN":
                return "Forhåndstilsagn";
            case "INGEN":
                return "Ingen utbetaling er valgt";
        }
    };
    return (
        <FormSummary>
            <FormSummary.Header>
                <FormSummary.Heading level="2">Sak {sak.saksnummer}</FormSummary.Heading>
            </FormSummary.Header>
            <FormSummary.Answers>
                <FormSummary.Answer>
                    <FormSummary.Label>Sakstatus</FormSummary.Label>
                    <FormSummary.Value>{getSakStatusNavn(sak.status)}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Stønad</FormSummary.Label>
                    <FormSummary.Value>{getStonadsTypeNavn(sak.type)}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Beskrivelse</FormSummary.Label>
                    <FormSummary.Value>{sak.beskrivelse}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Søknadsdato</FormSummary.Label>
                    <FormSummary.Value>{isoTilLokal(sak.soknadsDato)}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Tildelingsår</FormSummary.Label>
                    <FormSummary.Value>{sak.tildelingsAar}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Begrunnelse</FormSummary.Label>
                    <FormSummary.Value>{sak.begrunnelse || "Ingen"}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Saksbehandler</FormSummary.Label>
                    <FormSummary.Value>
                        {sak.saksbehandler.navn} <small>({sak.saksbehandler.navIdent})</small>
                    </FormSummary.Value>
                </FormSummary.Answer>
                {sak.attestant &&<FormSummary.Answer>
                    <FormSummary.Label>Attestant</FormSummary.Label>
                    <FormSummary.Value>
                        {sak.attestant.navn} <small>({sak.attestant.navIdent})</small>
                    </FormSummary.Value>
                </FormSummary.Answer>}

                <FormSummary.Answer>
                    <FormSummary.Label>Resultat</FormSummary.Label>
                    <FormSummary.Value>
                        <FormSummary.Answers>
                            <FormSummary.Answer>
                                <FormSummary.Label>Vedtaksresultat</FormSummary.Label>
                                <FormSummary.Value>{getSakVedtakNavn(sak.vedtaksResultat)}</FormSummary.Value>
                            </FormSummary.Answer>
                            <FormSummary.Answer>
                                <FormSummary.Label>Type utbetaling</FormSummary.Label>
                                <FormSummary.Value>{utbetalingsTypeText(sak.utbetalingsType)}</FormSummary.Value>
                            </FormSummary.Answer>

                            {sak.utbetalingsType === "BRUKER" && (
                                <FormSummary.Answer>
                                    <FormSummary.Label>Utbetalingsbeløp</FormSummary.Label>
                                    <FormSummary.Value>{sak.belop} kr</FormSummary.Value>
                                </FormSummary.Answer>
                            )}
                        </FormSummary.Answers>
                    </FormSummary.Value>
                </FormSummary.Answer>
            </FormSummary.Answers>
        </FormSummary>
    );
}
