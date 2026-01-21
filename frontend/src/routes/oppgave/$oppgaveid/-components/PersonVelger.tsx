import {PersonIcon} from '@navikt/aksel-icons'
import {BodyShort, Button, HStack, Label, Loader, TextField, VStack} from '@navikt/ds-react'
import {useState} from 'react'
import {findPersonByFnr as findPerson} from "@generated";


export type PersonValue = {
    fnr: string
    navn?: string
}

interface Props {
   name: string
   label: string
   value: PersonValue
   error?: string
   onChange?: (value: PersonValue) => void
}

export function PersonVelger(props: Props) {
   const [editMode, setEditmode] = useState(!props.value?.navn)
   const [inputFnr, setInputFnr] = useState(props.value?.fnr ?? '')

    async function doSearch(fnr: string) {

        const {data, error} = await findPerson({
                body: {fnr: fnr}
            }
        )
        if (error) {
            console.error("Noe gikk galt " + error)
            return
        }

    }

   const navn = props.value?.navn
   const fnr = props.value?.fnr ?? ''



   const handleEnterEditMode = () => {
      setEditmode(true)
      setInputFnr(props.value.fnr)
      props.onChange?.({ fnr: props.value.fnr, navn: undefined })
   }

   const sokPerson = async (fnr: string) => {
       const {data, error} = await findPerson({
               body: {fnr: fnr}
           }
       )
   }

   const handleSokPerson = () => {
      sokPerson(inputFnr)
   }

   const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Enter') {
         e.preventDefault()
         sokPerson(inputFnr)
      }
   }

   // // biome-ignore lint/correctness/useExhaustiveDependencies: onChange kan føre til uendelig loop
   // useEffect(
   //    function oppdaterValgtPerson() {
   //       if (!fetcherError && person) {
   //          setEditmode(false)
   //          props.onChange?.(person)
   //       }
   //    },
   //    [fetcherError, person],
   // )

    const isSubmitting= false

   return (
      <VStack gap="2">
         <input type="hidden" name={props.name} value={fnr} />
         {isSubmitting ? (
            <>
               <Label>{props.label}</Label>
               <Loader height="3rem" />
            </>
         ) : editMode || !navn ? (
            <div>
               <HStack align="center" justify="space-between" gap="4">
                  <TextField
                     label={props.label}
                     inputMode="search"
                     value={inputFnr}
                     onChange={(e) => setInputFnr(e.currentTarget.value)}
                     error={props.error}
                     maxLength={11}
                     onKeyDown={handleKeyDown}
                  />
                  <Button variant={'primary'} type={'button'} size="small" onClick={handleSokPerson}>
                     Velg
                  </Button>
               </HStack>
            </div>
         ) : (
            <>
               <Label>{props.label}</Label>
               <HStack height="3rem" gap="4" align={'start'} justify="space-between">
                  <HStack gap="2" align="center">
                     <PersonIcon title="person" fontSize="1.5rem" />
                     <BodyShort>
                        {navn}/{fnr}
                     </BodyShort>
                  </HStack>
                  <Button variant={'secondary'} type={'button'} size="small" onClick={handleEnterEditMode}>
                     Endre
                  </Button>
               </HStack>
            </>
         )}
      </VStack>
   )
}
