import {TextField, VStack} from '@navikt/ds-react'
import {AnnetInnholdCombox} from './AnnetInnholdCombobox'

interface Props {
   dokument: { dokumentInfoId: string; tittel?: string }
   error?: string
   showAnnetInnhold?: boolean
}

function getDokumentLabel(dokumentInfoId: string): string {
   const match = dokumentInfoId.match(/-?(\d+)$/)
   const number = match ? Number.parseInt(match[1], 10) : 0
   return number > 1 ? `Dokumenttittel ${number}` : 'Dokumenttittel'
}

export function DokumentTittelFelt({ dokument, error, showAnnetInnhold = false }: Props) {
   return (
      <VStack gap="space-16">
         <TextField
            label={getDokumentLabel(dokument.dokumentInfoId)}
            name={`dokumenttittel_${dokument.dokumentInfoId}`}
            minLength={5}
            error={error}
            defaultValue={dokument.tittel}
         />
         {showAnnetInnhold && <AnnetInnholdCombox name="annetInnhold" />}
      </VStack>
   )
}
