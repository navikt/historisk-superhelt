import type {
    JournalforDokument,
    JournalforRequest,
    Journalpost,
    OppgaveMedSak,
    Person,
    ProblemDetail,
    Sak,
} from "@generated";
import {journalforMutation} from "@generated/@tanstack/react-query.gen";
import {Button, Radio, RadioGroup, VStack} from "@navikt/ds-react";
import {useMutation} from "@tanstack/react-query";
import {useNavigate} from "@tanstack/react-router";
import {useState} from "react";
import {Card} from "~/common/card/Card";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {hasSize, isValidFnr} from "~/common/validation.utils";
import {AnnetInnholdCombobox} from "~/routes/oppgave/$oppgaveid/-components/AnnetInnholdCombobox";
import {EksisterendeSakVelger} from "~/routes/oppgave/$oppgaveid/-components/EksisterendeSakVelger";
import type {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {DokumentTittelFelt} from "./DokumentTittelFelt";
import {type PersonValue, PersonVelger} from "./PersonVelger";
import {StonadsTypeVelger} from "./StonadsTypeVelger";

type SakModus = "ny" | "eksisterende";

interface DokumentError {
    dokumentInfoId: string;
    tittel?: string;
}

interface FormErrors {
    bruker?: string;
    avsender?: string;
    dokumenter?: DokumentError[];
    stonadstype?: string;
    fagsaksnummer?: string;
}

interface Props {
    person: Person;
    oppgaveMedSak: OppgaveMedSak;
    journalPost: Journalpost;
    defaultStonadstype?: StonadType;
    onBrukerUpdate: (updated: Person) => void;
    readOnly?: boolean;
}

export function JournalforForm({
                                   person,
                                   oppgaveMedSak,
                                   journalPost,
                                   defaultStonadstype,
                                   onBrukerUpdate,
                                   readOnly,
                               }: Props) {
    const navigate = useNavigate();
    const [backendError, setBackendError] = useState<ProblemDetail | undefined>();
    const journalfor = useMutation({
        ...journalforMutation(),
        onError: (error) => {
            setBackendError(error);
        },
    });
    const [sakModus, setSakModus] = useState<SakModus>(defaultStonadstype ? "ny" : "eksisterende");
    const [valgtSak, setValgtSak] = useState<Sak | undefined>();
    const [stonadstype, setStonadstype] = useState<StonadType | undefined>(defaultStonadstype);
    const [bruker, setBruker] = useState<PersonValue>({fnr: person.fnr, navn: person.navn});
    const [avsender, setAvsender] = useState<PersonValue>({
        fnr:
            journalPost?.avsenderMottaker?.id ||
            (person.harVerge && person.vergeInfo ? person.vergeInfo.fnr : person.fnr),
        navn:
            journalPost?.avsenderMottaker?.navn ||
            (person.harVerge && person.vergeInfo ? person.vergeInfo?.navn : person.navn),
    });
    const [logiskeVedlegg, setLogiskeVedlegg] = useState<string[]>([]);

    const [validationErrors, setValidationErrors] = useState<FormErrors>({});

    const handleBrukerChange = (value: PersonValue) => {
        setBruker(value);
        if (value.fnr && value.navn) {
            onBrukerUpdate({fnr: value.fnr, navn: value.navn} as Person);
            setValidationErrors((prev) => ({...prev, bruker: undefined}));
        }
    };

    const handleAvsenderChange = (value: PersonValue) => {
        setAvsender(value);
        if (value.fnr && value.navn) {
            setValidationErrors((prev) => ({...prev, avsender: undefined}));
        }
    };

    async function validateAndSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (readOnly) {
            return;
        }
        const formData = new FormData(e.currentTarget);
        // TODO se om dette kan forenkles ved å bruke state direkte
        const dokumenter: JournalforDokument[] = [];
        for (const [key, value] of formData.entries()) {
            if (key.startsWith("dokumenttittel_")) {
                const dokumentInfoId = key.replace("dokumenttittel_", "");
                dokumenter.push({
                    dokumentInfoId,
                    tittel: value as string,
                    logiskeVedlegg: logiskeVedlegg,
                });
            }
        }

        const errors: FormErrors = {};

        if (sakModus === "ny" && !stonadstype) {
            errors.stonadstype = "Stønadstype er påkrevd";
        }
        if (sakModus === "eksisterende" && !valgtSak) {
            errors.fagsaksnummer = "Du må velge en eksisterende sak";
        }
        if (!isValidFnr(bruker.fnr)) {
            errors.bruker = "Bruker må være et gyldig fødselsnummer";
        }
        if (!bruker.navn) {
            errors.bruker = "Du må velge en person før du kan journalføre";
        }
        if (!isValidFnr(avsender.fnr)) {
            errors.avsender = "Avsender må være et gyldig fødselsnummer";
        }
        if (!avsender.navn) {
            errors.avsender = "Du må velge en person før du kan journalføre";
        }

        const dokumentErrors = dokumenter
            ?.filter((d) => !hasSize(d.tittel, 5))
            .map((d) => ({dokumentInfoId: d.dokumentInfoId, tittel: "Tittel må være minst 5 tegn"}));
        if (dokumentErrors && dokumentErrors.length > 0) {
            errors.dokumenter = dokumentErrors;
        }

        setValidationErrors(errors);
        if (Object.keys(errors).length > 0) {
            return;
        }
        const jfrRequest: JournalforRequest = {
            jfrOppgaveId: oppgaveMedSak.oppgaveId,
            bruker: bruker.fnr,
            avsender: avsender.fnr,
            stonadsType: (sakModus === "eksisterende" ? valgtSak!.type : stonadstype!) as StonadType,
            dokumenter: dokumenter,
            // TODO: backend må støtte fagsaksnummer i JournalforRequest
            ...(sakModus === "eksisterende" && valgtSak ? {fagsaksnummer: valgtSak.saksnummer} : {}),
        };
        await sendToBackend(jfrRequest);
    }

    async function sendToBackend(jfrRequest: JournalforRequest) {
        const saksnummer = await journalfor.mutateAsync({
            path: {
                journalpostId: journalPost.journalpostId,
            },
            body: jfrRequest,
        });
        await navigate({to: "/sak/$saksnummer", params: {saksnummer}});
    }

    return (
        <form onSubmit={validateAndSubmit}>
            <VStack gap="space-24">
                <input type="hidden" name="journalpostId" value={journalPost.journalpostId}/>
                <input type="hidden" name="jfrOppgave" value={oppgaveMedSak.oppgaveId}/>
                <Card title={"Bruker og avsender"}>
                    <PersonVelger
                        label="Bruker"
                        name="bruker"
                        value={bruker}
                        readOnly={readOnly}
                        error={validationErrors?.bruker}
                        onChange={handleBrukerChange}
                    />
                    <PersonVelger
                        label="Avsender"
                        name="avsender"
                        value={avsender}
                        readOnly={readOnly}
                        error={validationErrors?.avsender}
                        onChange={handleAvsenderChange}
                    />
                </Card>
                <Card title="Dokumenter">
                    {journalPost?.dokumenter?.map((dok, index: number) => (
                        <div key={dok.dokumentInfoId}>
                            <DokumentTittelFelt
                                index={index}
                                value={dok.tittel}
                                readOnly={readOnly}
                                name={`dokumenttittel_${dok.dokumentInfoId}`}
                                error={
                                    validationErrors?.dokumenter?.find((d) => d.dokumentInfoId === dok.dokumentInfoId)
                                        ?.tittel
                                }
                            />
                            {index === 0 && (
                                <AnnetInnholdCombobox
                                    name="annetInnhold"
                                    onChange={setLogiskeVedlegg}
                                    readOnly={readOnly}
                                />
                            )}
                        </div>
                    ))}
                </Card>

                <Card title="Sak">
                    <RadioGroup
                        legend="Knytt journalpost til"
                        value={sakModus}
                        readOnly={readOnly}
                        onChange={(value) => {
                            setSakModus(value as SakModus);
                            setValgtSak(undefined);
                            setStonadstype(defaultStonadstype);
                            setValidationErrors((prev) => ({
                                ...prev,
                                stonadstype: undefined,
                                fagsaksnummer: undefined,
                            }));
                        }}
                    >
                        <Radio value="ny">Ny sak</Radio>
                        <Radio value="eksisterende">Eksisterende sak</Radio>
                    </RadioGroup>
                    {sakModus === "ny" && (
                        <StonadsTypeVelger
                            name="stonadstype"
                            value={stonadstype}
                            error={validationErrors?.stonadstype}
                            onChange={setStonadstype}
                            readOnly={readOnly}
                        />
                    )}
                    {sakModus === "eksisterende" && (
                        <EksisterendeSakVelger
                            maskertPersonIdent={oppgaveMedSak.maskertPersonIdent}
                            valgtSaksnummer={valgtSak?.saksnummer}
                            error={validationErrors?.fagsaksnummer}
                            readOnly={readOnly}
                            onVelgSak={(sak) => {
                                setValgtSak(sak);
                                setStonadstype(sak.type as StonadType);
                                setValidationErrors((prev) => ({...prev, fagsaksnummer: undefined}));
                            }}
                        />
                    )}
                </Card>
                
                <Button type="submit" disabled={readOnly}>
                    {sakModus === "eksisterende" ? "Journalfør på eksisterende sak" : "Journalfør og start behandling"}
                </Button>

                {backendError && <ErrorAlert error={backendError}/>}
            </VStack>
        </form>
    );
}
