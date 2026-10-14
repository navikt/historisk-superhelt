export interface Brevdata {
    behandlingsnummer: string;
    personalia: { ident: string; fornavn: string; etternavn: string };
    datoForUtsending: string;
    saksbehandlerNavn: string;
    beslutterNavn?: string | null;
    kontor: string;
    innhold_typst: string;
    brevtype: string;
    mottaker: string;
    målform: string;
}

export function brevdata(overrides: Partial<Brevdata> = {}): Brevdata {
    return {
        behandlingsnummer: "TEST-123",
        personalia: { ident: "12345678901", fornavn: "Test", etternavn: "Testesen" },
        datoForUtsending: "14. oktober 2026",
        saksbehandlerNavn: "Test Saksbehandler",
        beslutterNavn: "Test Beslutter",
        kontor: "Nav Testkontor",
        innhold_typst: "= Testbrev\n\nDette er redigerbart innhold.",
        brevtype: "VEDTAKSBREV",
        mottaker: "BRUKER",
        målform: "NB",
        ...overrides,
    };
}
