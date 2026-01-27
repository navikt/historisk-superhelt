/**
 * Util metoder for validering
 */

export function isValidFnr(value?: string): boolean {
   if (!value?.trim()) {
      return false
   }
   return value.match('^[0-9]{11}$') !== null
}

export function hasSize(value?: string, min?: number, max?: number): boolean {
   return !!value && (!min || value.length >= min) && (!max || value.length <= max)
}
