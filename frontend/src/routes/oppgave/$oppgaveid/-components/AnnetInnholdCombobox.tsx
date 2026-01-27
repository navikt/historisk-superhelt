import { UNSAFE_Combobox } from '@navikt/ds-react'
import { useState } from 'react'

interface Props {
   onChange?: (selectedOptions: Array<string>) => void
   name?: string
   defaultValue?: Array<string>
}

export function AnnetInnholdCombobox({ onChange, name, defaultValue = [] }: Props) {
   const [selectedOptions, setSelectedOptions] = useState<Array<string>>(defaultValue)

   const onToggleSelected = (option: string, isSelected: boolean) => {
      const updated = isSelected ? [...selectedOptions, option] : selectedOptions.filter((o) => o !== option)
      setSelectedOptions(updated)
      if (onChange) onChange(updated)
   }

   return (
      <>
         <UNSAFE_Combobox
            allowNewValues
            label="Annet innhold"
            isMultiSelect
            onToggleSelected={onToggleSelected}
            selectedOptions={selectedOptions}
            options={initialOptions}
            shouldAutocomplete
         />
         {name && <input type="hidden" name={name} value={JSON.stringify(selectedOptions)} />}
      </>
   )
}

const initialOptions = ['Uttalelse fra legespesialist', 'Uttalelse fra ortopediingeniør']
