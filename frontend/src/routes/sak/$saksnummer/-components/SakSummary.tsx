import {FormSummary} from '@navikt/ds-react';
import {Sak} from '@api';

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
                    <FormSummary.Label>Status og vedtak</FormSummary.Label>
                    <FormSummary.Value>{sak.status} / {sak.vedtak}</FormSummary.Value>
                </FormSummary.Answer>

                <FormSummary.Answer>
                    <FormSummary.Label>Utbetaling</FormSummary.Label>
                    <FormSummary.Value>{sak.utbetalingsType}</FormSummary.Value>
                    <FormSummary.Value>{sak.utbetaling?.belop} kr</FormSummary.Value>
                </FormSummary.Answer>
                <FormSummary.Answer>
                    <FormSummary.Label>Begrunnelse</FormSummary.Label>
                    <FormSummary.Value>{sak.begrunnelse || 'Ingen'}</FormSummary.Value>
                </FormSummary.Answer>
            </FormSummary.Answers>
        </FormSummary>
    );
}
