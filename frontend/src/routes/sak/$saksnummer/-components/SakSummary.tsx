import {FormSummary} from '@navikt/ds-react';
import {Sak} from '@generated';

interface SakSummaryProps {
    sak: Sak;
}

export default function SakSummary({sak}: SakSummaryProps) {
    return (
        <FormSummary>
            <FormSummary.Header>
                <FormSummary.Heading level="2">Sak {sak.saksnummer}</FormSummary.Heading>
            </FormSummary.Header>
            <FormSummary.Answers>

                <FormSummary.Answer>
                    <FormSummary.Label>Type</FormSummary.Label>
                    <FormSummary.Value>{sak.type}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Tittel</FormSummary.Label>
                    <FormSummary.Value>{sak.tittel}</FormSummary.Value>
                </FormSummary.Answer>

                <FormSummary.Answer>
                    <FormSummary.Label>Søknadsdato</FormSummary.Label>
                    <FormSummary.Value>{sak.soknadsDato}</FormSummary.Value>
                </FormSummary.Answer>

                <FormSummary.Answer>
                    <FormSummary.Label>Tildelingsår</FormSummary.Label>
                    <FormSummary.Value>{sak.tildelingsAar}</FormSummary.Value>
                </FormSummary.Answer>

                <FormSummary.Answer>
                    <FormSummary.Label>Status og vedtak</FormSummary.Label>
                    <FormSummary.Value>{sak.status} / {sak.vedtaksResultat}</FormSummary.Value>
                </FormSummary.Answer>

                {sak.utbetaling && <FormSummary.Answer>
                    <FormSummary.Label>Utbetaling til bruker</FormSummary.Label>
                    <FormSummary.Value>{sak.utbetaling?.belop} kr</FormSummary.Value>
                    <FormSummary.Value>{sak.utbetaling.utbetalingStatus.toLowerCase()} - {sak.utbetaling.utbetalingTidspunkt}</FormSummary.Value>
                </FormSummary.Answer>
                }
                {sak.forhandstilsagn && <FormSummary.Answer>
                    <FormSummary.Label>Forhåndstilsagn</FormSummary.Label>
                    <FormSummary.Value>Det er gitt forhåndstilsagn til å sende inn faktura</FormSummary.Value>
                    <FormSummary.Value>{sak.forhandstilsagn?.belop} kr</FormSummary.Value>
                </FormSummary.Answer>
                }
                <FormSummary.Answer>
                    <FormSummary.Label>Begrunnelse</FormSummary.Label>
                    <FormSummary.Value>{sak.begrunnelse || 'Ingen'}</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Saksbehandler</FormSummary.Label>
                    <FormSummary.Value>{sak.saksbehandler}</FormSummary.Value>
                </FormSummary.Answer>
            </FormSummary.Answers>
        </FormSummary>
    );
}
