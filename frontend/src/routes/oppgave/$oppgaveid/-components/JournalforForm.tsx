import {Button, Heading, VStack} from '@navikt/ds-react'
import {useState} from 'react'
import {StonadsTypeVelger} from './StonadsTypeVelger'
import {DokumentTittelFelt} from './DokumentTittelFelt'
import {type PersonValue, PersonVelger} from './PersonVelger'
import {Journalpost, OppgaveMedSak, Person} from "@generated";
import {useNavigate} from "@tanstack/react-router";
import {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";

interface DokumentError {
   dokumentInfoId: string
   tittel?: string
}

interface FormErrors {
   bruker?: string
   avsender?: string
   dokumenter?: DokumentError[]
   behandlingstype?: string
   fagsaksnummer?: string
}

interface Props {
   person: Person
   oppgaveMedSak: OppgaveMedSak
   journalPost: Journalpost
   errors?: FormErrors
   defaultStonadstype?: StonadType
   onBrukerUpdate: (updated: Person) => void
}

export function JournalforForm({
   person,
   oppgaveMedSak,
   journalPost,
   errors,
   defaultStonadstype,
   onBrukerUpdate,
}: Props) {
   const navigation = useNavigate()
   const [stonadstypeState, setStonadstypeState] = useState<StonadType | undefined>(defaultStonadstype)
   const [bruker, setBruker] = useState<PersonValue>({ fnr: person.fnr, navn: person.navn })
   const [avsender, setAvsender] = useState<PersonValue>({
      fnr: journalPost?.avsenderMottaker?.id || person.fnr,
      navn: journalPost?.avsenderMottaker?.navn || person.navn,
   })
   const [validationErrors, setValidationErrors] = useState<{ bruker?: string; avsender?: string }>({})

   const handleBrukerChange = (value: PersonValue) => {
      setBruker(value)
      if (value.fnr && value.navn) {
         onBrukerUpdate({ fnr: value.fnr, navn: value.navn } as Person)
         setValidationErrors((prev) => ({ ...prev, bruker: undefined }))
      }
   }

   const handleAvsenderChange = (value: PersonValue) => {
      setAvsender(value)
      if (value.fnr && value.navn) {
         setValidationErrors((prev) => ({ ...prev, avsender: undefined }))
      }
   }

   const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
      const errors: { bruker?: string; avsender?: string } = {}

      if (!bruker.navn) {
         errors.bruker = 'Du må velge en person før du kan journalføre'
      }
      if (!avsender.navn) {
         errors.avsender = 'Du må velge en person før du kan journalføre'
      }

      if (Object.keys(errors).length > 0) {
         e.preventDefault()
         setValidationErrors(errors)
         return
      }

      setValidationErrors({})
   }

   return (
      <form method="POST"  onSubmit={handleSubmit}>
         <VStack gap="space-24">
            <input type="hidden" name="journalpostId" value={journalPost.journalpostId} />
            <input type="hidden" name="jfrOppgave" value={oppgaveMedSak.oppgaveId} />

            <PersonVelger
               label="Bruker"
               name="bruker"
               value={bruker}
               error={errors?.bruker || validationErrors.bruker}
               onChange={handleBrukerChange}
            />
            <PersonVelger
               label="Avsender"
               name="avsender"
               value={avsender}
               error={errors?.avsender || validationErrors.avsender}
               onChange={handleAvsenderChange}
            />

            <Heading level="3" size={'medium'}>
               Dokumenter
            </Heading>
            {journalPost?.dokumenter?.map((dok, index: number) => (
               <DokumentTittelFelt
                  key={dok.dokumentInfoId}
                  dokument={dok}
                  error={errors?.dokumenter?.find((d) => d.dokumentInfoId === dok.dokumentInfoId)?.tittel}
                  showAnnetInnhold={index === 0}
               />
            ))}

            <Heading level="3" size={'medium'}>
               Sak
            </Heading>
            <StonadsTypeVelger
               name="stonadstype"
               value={stonadstypeState}
               error={errors?.behandlingstype}
               onChange={setStonadstypeState}
            />
            <Button type="submit">
               Journalfør og start behandling
            </Button>
         </VStack>
      </form>
   )
}
