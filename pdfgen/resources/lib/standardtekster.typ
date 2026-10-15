#let innsynsrett = [
  == Du har rett til innsyn i saken din

  Du har rett til å se dokumentene i saken din. Dette følger av forvaltningsloven § 18.
  Kontakt oss om du vil se dokumentene i saken din. Ta kontakt på nav.no/kontakt eller på telefon 55 55 33 33.
  Du kan lese mer om innsynsretten på nav.no/personvernerklaering.
]

#let innsynsrett_NN = [
  == Du har rett til innsyn i saka di

  Du har rett til å sjå dokumenta i saka di. Dette følger av forvaltningslova § 18.
  Kontakt oss dersom du vil sjå dokumenta i saka di. Ta kontakt på nav.no/kontakt eller telefon 55 55 33 33.
  Du kan lese meir om innsynsretten på nav.no/personvernerklaering.
]

#let klagerett = [
  == Du kan klage på vedtaket

  Hvis du mener vedtaket er feil, kan du klage innen 6 uker fra den datoen vedtaket har kommet fram til deg.
  Dette følger av folketrygdloven § 21-12. Du finner skjema og informasjon på nav.no/klage.

  Nav kan veilede deg på telefon om hvordan du sender en klage. Nav-kontoret ditt kan også hjelpe deg med å skrive en klage.
  Kontakt oss på telefon 55 55 33 33 hvis du trenger hjelp.

  Hvis du får medhold i klagen, kan du få dekket vesentlige utgifter som har vært nødvendige for å få endret vedtaket, for eksempel hjelp fra advokat.
  Du kan ha krav på fri rettshjelp etter rettshjelploven. Du kan få mer informasjon om denne ordningen hos advokater, statsforvalteren eller Nav.

  Du kan lese om saksomkostninger i forvaltningsloven § 36.

  Hvis du sender klage i posten, må du signere klagen.

  Mer informasjon om klagerettigheter finner du på nav.no/klagerettigheter.
]

#let klagerett_NN = [
  == Du kan klage på vedtaket

  Dersom du meiner vedtaket er feil, kan du klage innan seks veker frå vedtaket kom fram til deg.
  Dette følger av folketrygdlova § 21-12. Skjema og informasjon finn du på nav.no/klage.

  Nav kan rettleie deg på telefon om korleis du sender ei klage. Nav-kontoret ditt kan òg hjelpe deg med å skrive
  klaga.
  Ta kontakt på telefon 55 55 33 33 viss du treng hjelp.

  Dersom du får medhald i klaga, kan du få dekt vesentlege utgifter som har vore nødvendige for å få endra vedtaket,
  til dømes hjelp frå advokat.
  Du kan ha krav på fri rettshjelp etter rettshjelplova. Du får meir informasjon om ordninga hos advokatar,
  statsforvaltaren eller Nav.

  Du kan lese om saksomkostningar i forvaltningslova § 36.

  Dersom du sender klage i posten, må du signere klaga.

  Meir informasjon om klagerettar finn du på nav.no/klagerettigheter.
]

#let personopplysningsrett = [
  == Du har rettigheter knyttet til personopplysningene dine

  Du finner informasjon om hvordan Nav behandler personopplysningene dine, og hvilke rettigheter du har, på
  nav.no/personvernerklaering\#hvordan.

  Nav kan veilede deg på telefon 55 55 33 33 om hvordan Nav behandler personopplysninger.
]

#let personopplysningsrett_NN = [
  == Du har rettar knytte til personopplysningane dine

  Du finn informasjon om korleis Nav behandlar personopplysningane dine, og kva rettar du har, på
  nav.no/personvernerklaering\#hvordan.

  Nav kan rettleie deg på telefon 55 55 33 33 om korleis Nav behandlar personopplysningar.
]

#let sporsmal = [
  == Har du spørsmål?

  Du finner mer informasjon på nav.no.
  På nav.no/kontakt kan du chatte eller skrive til oss.
  Hvis du ikke finner svar på nav.no kan du ringe oss på telefon 55 55 33 33, hverdager 09.00-15.00.
]

#let sporsmal_NN = [
  == Har du spørsmål?

  Du finn meir informasjon på nav.no.
  På nav.no/kontakt kan du chatte eller skrive til oss.
  Dersom du ikkje finn svar på nav.no kan du ringe oss på telefon 55 55 33 33, kvardagar 09.00–15.00.
]

#let veiledningsrett = [
  == Du har rett til å få veiledning fra Nav

  Vi har plikt til å veilede deg om dine rettigheter og plikter i saken din, både før, under og etter saksbehandlingen.
  Dette følger av forvaltningsloven § 11. Ta kontakt på telefon 55 55 33 33 eller på nav.no/kontakt hvis du har spørsmål.
]

#let veiledningsrett_NN = [
  == Du har rett til rettleiing frå Nav

  Vi har plikt til å rettleie deg om rettane og pliktene dine i saka, både før, under og etter saksbehandlinga.
  Dette følger av forvaltningslova § 11. Ta kontakt på telefon 55 55 33 33 eller på nav.no/kontakt dersom
  du har spørsmål.
]

#let standardtekster(nynorsk) = if nynorsk {
  (
    klagerett: klagerett_NN,
    innsynsrett: innsynsrett_NN,
    personopplysningsrett: personopplysningsrett_NN,
    veiledningsrett: veiledningsrett_NN,
    sporsmal: sporsmal_NN,
  )
} else {
  (
    klagerett: klagerett,
    innsynsrett: innsynsrett,
    personopplysningsrett: personopplysningsrett,
    veiledningsrett: veiledningsrett,
    sporsmal: sporsmal,
  )
}
