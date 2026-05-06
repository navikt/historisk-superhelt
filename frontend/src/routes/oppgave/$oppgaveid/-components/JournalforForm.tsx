import type {JournalforDokument, Journalpost, OppgaveMedSak, Person} from "@generated";
import {Radio, RadioGroup, VStack} from "@navikt/ds-react";
import {useRef, useState} from "react";
import {Card} from "~/common/card/Card";
import type {StonadType} from "~/common/sak/sak.types";
import {hasSize, isValidFnr} from "~/common/validation.utils";
import {AnnetInnholdCombobox} from "./AnnetInnholdCombobox";
import {DokumentTittelFelt} from "./DokumentTittelFelt";
import {EksisterendeSakAction} from "./EksisterendeSakAction";
import {NySakAction} from "./NySakAction";
import {type PersonValue, PersonVelger} from "./PersonVelger";

type SakModus = "ny" | "eksisterende";

export interface FellesData {
    bruker: string;
    avsender: string;
    dokumenter: JournalforDokument[];
}

interface Errors {
    bruker?: string;
    avsender?: string;
    dokumenter?: Array<{ dokumentInfoId: string; tittel?: string }>;
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
    const formRef = useRef<HTMLFormElement>(null);
    const [modus, setModus] = useState<SakModus>("ny");
    const [bruker, setBruker] = useState<PersonValue>({ fnr: person.fnr, navn: person.navn });
    const [avsender, setAvsender] = useState<PersonValue>({
        fnr:
            journalPost?.avsenderMottaker?.id ||
            (person.harVerge && person.vergeInfo ? person.vergeInfo.fnr : person.fnr),
        navn:
            journalPost?.avsenderMottaker?.navn ||
            (person.harVerge && person.vergeInfo ? person.vergeInfo?.navn : person.navn),
    });
    const [logiskeVedlegg, setLogiskeVedlegg] = useState<string[]>([]);
    const [errors, setErrors] = useState<Errors>({});

    function getCommonData(): FellesData | undefined {
        if (!formRef.current) return undefined;
        const formData = new FormData(formRef.current);

        const dokumenter: JournalforDokument[] = [];
        for (const [key, value] of formData.entries()) {
            if (key.startsWith("dokumenttittel_")) {
                dokumenter.push({
                    dokumentInfoId: key.replace("dokumenttittel_", ""),
                    tittel: value as string,
                    logiskeVedlegg,
                });
            }
        }

        const fellesErrors: Errors = {};
        if (!isValidFnr(bruker.fnr)) fellesErrors.bruker = "Bruker må være et gyldig fødselsnummer";
        if (!bruker.navn) fellesErrors.bruker = "Du må velge en person før du kan journalføre";
        if (!isValidFnr(avsender.fnr)) fellesErrors.avsender = "Avsender må være et gyldig fødselsnummer";
        if (!avsender.navn) fellesErrors.avsender = "Du må velge en person før du kan journalføre";
        const dokumentErrors = dokumenter
            .filter((d) => !hasSize(d.tittel, 5))
            .map((d) => ({ dokumentInfoId: d.dokumentInfoId, tittel: "Tittel må være minst 5 tegn" }));
        if (dokumentErrors.length > 0) fellesErrors.dokumenter = dokumentErrors;

        if (Object.keys(fellesErrors).length > 0) {
            setErrors(fellesErrors);
            return undefined;
        }

        setErrors({});
        return { bruker: bruker.fnr, avsender: avsender.fnr, dokumenter };
    }

    return (
        <form ref={formRef} onSubmit={(e) => e.preventDefault()}>
            <VStack gap="space-24">
                <Card tittel="Bruker og avsender">
                    <PersonVelger
                        label="Bruker"
                        name="bruker"
                        value={bruker}
                        readOnly={readOnly}
                        error={errors.bruker}
                        onChange={(v) => {
                            setBruker(v);
                            if (v.fnr && v.navn) {
                                onBrukerUpdate({ fnr: v.fnr, navn: v.navn } as Person);
                                setErrors((p) => ({ ...p, bruker: undefined }));
                            }
                        }}
                    />
                    <PersonVelger
                        label="Avsender"
                        name="avsender"
                        value={avsender}
                        readOnly={readOnly}
                        error={errors.avsender}
                        onChange={(v) => {
                            setAvsender(v);
                            if (v.fnr && v.navn) setErrors((p) => ({ ...p, avsender: undefined }));
                        }}
                    />
                </Card>
                <Card tittel="Dokumenter">
                    {journalPost?.dokumenter?.map((dok, index) => (
                        <VStack key={dok.dokumentInfoId} gap={"space-8"}>
                            <DokumentTittelFelt
                                index={index}
                                value={dok.tittel}
                                readOnly={readOnly}
                                name={`dokumenttittel_${dok.dokumentInfoId}`}
                                error={errors.dokumenter?.find((d) => d.dokumentInfoId === dok.dokumentInfoId)?.tittel}
                            />
                            {index === 0 && (
                                <AnnetInnholdCombobox
                                    name="annetInnhold"
                                    onChange={setLogiskeVedlegg}
                                    readOnly={readOnly}
                                />
                            )}
                        </VStack>
                    ))}
                </Card>
                <Card tittel="Sak">
                    <RadioGroup
                        legend="Knytt journalpost til"
                        value={modus}
                        readOnly={readOnly}
                        onChange={(v) => setModus(v as SakModus)}
                    >
                        <Radio value="ny">Ny sak</Radio>
                        <Radio value="eksisterende">Eksisterende sak</Radio>
                    </RadioGroup>
                    {modus === "ny" ? (
                        <NySakAction
                            oppgaveMedSak={oppgaveMedSak}
                            journalPost={journalPost}
                            defaultStonadstype={defaultStonadstype}
                            readOnly={readOnly}
                            getCommonData={getCommonData}
                        />
                    ) : (
                        <EksisterendeSakAction
                            oppgaveMedSak={oppgaveMedSak}
                            journalPost={journalPost}
                            readOnly={readOnly}
                            getCommonData={getCommonData}
                        />
                    )}
                </Card>
            </VStack>
        </form>
    );
}
