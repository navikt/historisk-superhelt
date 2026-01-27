import {Button, Heading, VStack} from '@navikt/ds-react'
import {useState} from 'react'
import {StonadsTypeVelger} from './StonadsTypeVelger'
import {DokumentTittelFelt} from './DokumentTittelFelt'
import {type PersonValue, PersonVelger} from './PersonVelger'
import {JournalforDokument, JournalforRequest, Journalpost, OppgaveMedSak, Person, ProblemDetail} from "@generated";
import {useNavigate} from "@tanstack/react-router";
import {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {hasSize, isValidFnr} from "~/common/validation.utils";
import {AnnetInnholdCombobox} from "~/routes/oppgave/$oppgaveid/-components/AnnetInnholdCombobox";
import {useMutation} from "@tanstack/react-query";
import {journalforMutation} from "@generated/@tanstack/react-query.gen";
import {ErrorAlert} from "~/common/error/ErrorAlert";


interface DokumentError {
    dokumentInfoId: string
    tittel?: string
}

interface FormErrors {
    bruker?: string
    avsender?: string
    dokumenter?: DokumentError[]
    stonadstype?: string
    fagsaksnummer?: string
}

interface Props {
    person: Person
    oppgaveMedSak: OppgaveMedSak
    journalPost: Journalpost
    defaultStonadstype?: StonadType
    onBrukerUpdate: (updated: Person) => void
}

export function JournalforForm({
                                   person,
                                   oppgaveMedSak,
                                   journalPost,
                                   defaultStonadstype,
                                   onBrukerUpdate,
                               }: Props) {
    const navigate = useNavigate()
    const [backendError, setBackendError] = useState<ProblemDetail| undefined>()
    const journalfor = useMutation({
        ...journalforMutation(),
        onError: (error) => {
        setBackendError(error)
        }

    })
    const [stonadstype, setStonadstype] = useState<StonadType | undefined>(defaultStonadstype)
    const [bruker, setBruker] = useState<PersonValue>({fnr: person.fnr, navn: person.navn})
    const [avsender, setAvsender] = useState<PersonValue>({
        fnr: journalPost?.avsenderMottaker?.id || person.fnr,
        navn: journalPost?.avsenderMottaker?.navn || person.navn,
    })
    const [logiskeVedlegg, setLogiskeVedlegg] = useState<string[]>([])

    const [validationErrors, setValidationErrors] = useState<FormErrors>({})

    const handleBrukerChange = (value: PersonValue) => {
        setBruker(value)
        if (value.fnr && value.navn) {
            onBrukerUpdate({fnr: value.fnr, navn: value.navn} as Person)
            setValidationErrors((prev) => ({...prev, bruker: undefined}))
        }
    }

    const handleAvsenderChange = (value: PersonValue) => {
        setAvsender(value)
        if (value.fnr && value.navn) {
            setValidationErrors((prev) => ({...prev, avsender: undefined}))
        }
    }


    async function validateAndSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault()
        const formData = new FormData(e.currentTarget)
// TODO se om dette kan forenkles ved å bruke state direkte
        const dokumenter: JournalforDokument[] = []
        for (const [key, value] of formData.entries()) {
            if (key.startsWith('dokumenttittel_')) {
                const dokumentInfoId = key.replace('dokumenttittel_', '')
                dokumenter.push({
                    dokumentInfoId,
                    tittel: value as string,
                    logiskeVedlegg: logiskeVedlegg,
                })
            }
        }

        const errors: FormErrors = {}

        if (!stonadstype) {
            errors.stonadstype = 'Stønadstype er påkrevd'
        }
        if (!isValidFnr(bruker.fnr)) {
            errors.bruker = 'Bruker må være et gyldig fødselsnummer'
        }
        if (!bruker.navn) {
            errors.bruker = 'Du må velge en person før du kan journalføre'
        }
        if (!isValidFnr(avsender.fnr)) {
            errors.avsender = 'Avsender må være et gyldig fødselsnummer'
        }
        if (!avsender.navn) {
            errors.avsender = 'Du må velge en person før du kan journalføre'
        }

        const dokumentErrors = dokumenter
            ?.filter((d) => !hasSize(d.tittel, 5))
            .map((d) => ({dokumentInfoId: d.dokumentInfoId, tittel: 'Tittel må være minst 5 tegn'}))
        if (dokumentErrors && dokumentErrors.length > 0) {
            errors.dokumenter = dokumentErrors
        }


        setValidationErrors(errors)
        if (Object.keys(errors).length > 0) {
            return
        }
        const jfrRequest: JournalforRequest = {
            jfrOppgaveId: oppgaveMedSak.oppgaveId,
            bruker: bruker.fnr,
            avsender: avsender.fnr,
            stonadsType: stonadstype!,
            dokumenter: dokumenter,
        }
        await sendToBackend(jfrRequest);
    }

    async function sendToBackend(jfrRequest: JournalforRequest) {

        const saksnummer = await journalfor.mutateAsync({
            path: {
                journalpostId: journalPost.journalpostId
            },
            body: jfrRequest
        });
        await navigate({to: "/sak/$saksnummer", params: {saksnummer}})
    }

    return (
        <form onSubmit={validateAndSubmit}>
            <VStack gap="space-24">
                <input type="hidden" name="journalpostId" value={journalPost.journalpostId}/>
                <input type="hidden" name="jfrOppgave" value={oppgaveMedSak.oppgaveId}/>

                <PersonVelger
                    label="Bruker"
                    name="bruker"
                    value={bruker}
                    error={validationErrors?.bruker}
                    onChange={handleBrukerChange}
                />
                <PersonVelger
                    label="Avsender"
                    name="avsender"
                    value={avsender}
                    error={validationErrors?.avsender}
                    onChange={handleAvsenderChange}
                />

                <Heading level="3" size={'medium'}>
                    Dokumenter
                </Heading>
                {journalPost?.dokumenter?.map((dok, index: number) => (
                    <div
                        key={dok.dokumentInfoId}>
                        <DokumentTittelFelt
                            index={index}
                            value={dok.tittel}
                            name={`dokumenttittel_${dok.dokumentInfoId}`}
                            error={validationErrors?.dokumenter?.find((d) => d.dokumentInfoId === dok.dokumentInfoId)?.tittel}
                        />
                        {index === 0 && <AnnetInnholdCombobox name="annetInnhold" onChange={setLogiskeVedlegg}/>}
                    </div>
                ))}

                <Heading level="3" size={'medium'}>
                    Sak
                </Heading>
                <StonadsTypeVelger
                    name="stonadstype"
                    value={stonadstype}
                    error={validationErrors?.stonadstype}
                    onChange={setStonadstype}
                />
                <Button type="submit">
                    Journalfør og start behandling
                </Button>
                {backendError && (<ErrorAlert error={backendError}/>)}
            </VStack>
        </form>
    )
}
