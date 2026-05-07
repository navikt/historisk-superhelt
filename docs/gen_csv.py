import csv
import json

def klage_json(utfall):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440000","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"KLAGEBEHANDLING_AVSLUTTET","detaljer":{"klagebehandlingAvsluttet":{"avsluttet":"2026-04-10T10:30:00","utfall":utfall,"journalpostReferanser":["123456789","987654321"]}}}, ensure_ascii=False)

def anke_opprettet_json():
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440001","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"ANKEBEHANDLING_OPPRETTET","detaljer":{"ankebehandlingOpprettet":{"mottattKlageinstans":"2026-04-15T08:00:00"}}}, ensure_ascii=False)

def anke_avsluttet_json(utfall):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440002","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"ANKEBEHANDLING_AVSLUTTET","detaljer":{"ankebehandlingAvsluttet":{"avsluttet":"2026-05-01T14:00:00","utfall":utfall,"journalpostReferanser":["111222333"]}}}, ensure_ascii=False)

def trygderetten_json(utfall):
    d = {"sendtTilTrygderetten":"2026-05-10T09:00:00"}
    if utfall != "(utfall ikke satt)":
        d["utfall"] = utfall
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440003","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","detaljer":{"ankeITrygderettenbehandlingOpprettet":d}}, ensure_ascii=False)

def etter_trygderetten_json(utfall):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440004","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","detaljer":{"behandlingEtterTrygderettenOpphevetAvsluttet":{"avsluttet":"2026-06-01T10:00:00","utfall":utfall,"journalpostReferanser":["444555666"]}}}, ensure_ascii=False)

def omgjoering_json(utfall):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440005","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","detaljer":{"omgjoeringskravbehandlingAvsluttet":{"avsluttet":"2026-06-10T11:00:00","utfall":utfall,"journalpostReferanser":["777888999"]}}}, ensure_ascii=False)

def gjenopptak_json(utfall):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440006","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"GJENOPPTAKSBEHANDLING_AVSLUTTET","detaljer":{"gjenopptaksbehandlingAvsluttet":{"avsluttet":"2026-06-15T12:00:00","utfall":utfall,"journalpostReferanser":["112233445"]}}}, ensure_ascii=False)

def feilregistrert_json(behandling_type):
    return json.dumps({"eventId":"550e8400-e29b-41d4-a716-446655440007","kildeReferanse":"SH-000042","kilde":"SUPERHELT","kabalReferanse":"kabal-ref-abc123","type":"BEHANDLING_FEILREGISTRERT","detaljer":{"behandlingFeilregistrert":{"feilregistrert":"2026-04-20T09:15:00","navIdent":"Z123456","reason":"Feil person registrert paa saken","type":behandling_type}}}, ensure_ascii=False)

# Kolonner: Event-type | BehandlingDetaljer-felt | Utfall | Ekstra detaljer fra Kabal | Beskrivelse | Ankemulighet | Opprett oppgave? | Oppgavetype hvis JA | Prioritet | Kommentar | Eksempel JSON
HDR = ["Event-type","BehandlingDetaljer-felt","Utfall","Ekstra detaljer fra Kabal","Beskrivelse","Ankemulighet (JA/NEI)","Vedtaksinstans skal iverksette (JA/NEI)","Opprett oppgave? (JA/NEI/VURDER)","Oppgavetype hvis JA","Prioritet (HOY/NORMAL/LAV)","Kommentar (fagekspert fyller ut)","Eksempel JSON (fra Kabal)"]

def row(event, felt, utfall, ekstra, beskrivelse, ankemulighet, iverksette, *rest):
    padded = list(rest) + [""] * (4 - len(rest))
    return [event, felt, utfall, ekstra, beskrivelse, ankemulighet, iverksette] + padded

rows = [
    HDR,
    [""] * 11,

    # ── KLAGEBEHANDLING ──────────────────────────────────────────────────────────
    # Klage i Kabal: klagesak sendt fra vedtaksinstans til Klageinstans.
    ["=== KLAGEBEHANDLING (Klageinstans) ==="] + [""] * 11,
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser",
        "Klageinstans har registrert klagen som trukket av bruker.","NEI","NEI","","","","",klage_json("TRUKKET")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","RETUR","avsluttet + journalpostReferanser",
        "Klageinstans returnerer klagesaken uten avgjorelse pga. formelle feil (f.eks. feil instans eller mangelfull saksforberedelse, jfr. fvl. § 33). Feil maa rettes og saken sendes til Klageinstans paa nytt.","NEI","NEI","","","","",klage_json("RETUR")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","OPPHEVET","avsluttet + journalpostReferanser",
        "Klageinstans kan ikke vurdere saken fordi den ikke er godt nok utredet. Vedtaksenheten skal utrede saken og gjoere nytt vedtak. Saken skal ikke tilbake til Klageinstans med mindre bruker klager paa vedtaksenhetens nye vedtak.","NEI","NEI","","","","",klage_json("OPPHEVET")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt medhold. Vedtaksinstans skal iverksette.","JA","JA","","","","",klage_json("MEDHOLD")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt delvis medhold. Vedtaksinstans skal iverksette.","JA","JA","","","","",klage_json("DELVIS_MEDHOLD")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser",
        "Klageinstans har stadfestet vedtaksinstansens vedtak. Klager fikk ikke medhold.","JA","NEI","","","","",klage_json("STADFESTELSE")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser",
        "Vedtaksinstansens vedtak var ugyldig og er gjort om til brukers ugunst (Ugyldig).","JA","JA","","","","",klage_json("UGUNST")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","AVVIST","avsluttet + journalpostReferanser",
        "Klageinstans har avvist klagen. Typisk pga. oversittet klagefrist eller formfeil (f.eks. klagen er ikke signert).","JA","NEI","","","","",klage_json("AVVIST")),
    row("KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser",
        "Saken henlegges – behandles ikke videre. Skiller seg fra Trukket ved at det ikke er bruker som har trukket saken. Typisk: den saken gjelder er doed og dodsboet tar ikke saken videre.","NEI","NEI","","","","",klage_json("HENLAGT")),

    [""] * 11,

    # ── ANKEBEHANDLING (Klageinstans) ────────────────────────────────────────────
    # Anke: bruker har anket Klageinstansens vedtak. Anken fremsettes for Klageinstans,
    # som vurderer eget vedtak. Endres ikke vedtak, forberedes saken for Trygderetten.
    ["=== ANKEBEHANDLING (Klageinstans) ==="] + [""] * 11,
    row("ANKEBEHANDLING_OPPRETTET","ankebehandlingOpprettet","(ingen utfall)","mottattKlageinstans (dato)",
        "Anke er opprettet og mottatt i Klageinstans (Kabal).","–","–","","","","",anke_opprettet_json()),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser",
        "Klageinstans har registrert anken som trukket av bruker. Sendes ikke til Trygderetten.","NEI","NEI","","","","",anke_avsluttet_json("TRUKKET")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","OPPHEVET","avsluttet + journalpostReferanser",
        "Klageinstans kan ikke vurdere saken – ikke godt nok utredet. Vedtaksenheten skal utrede og gjoere nytt vedtak. Saken skal ikke tilbake til Klageinstans med mindre bruker klager paa nytt vedtak. Sendes ikke til Trygderetten.","NEI","NEI","","","","",anke_avsluttet_json("OPPHEVET")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt medhold i anken. Vedtaksinstans skal iverksette. Sendes ikke til Trygderetten.","JA","JA","","","","",anke_avsluttet_json("MEDHOLD")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt delvis medhold i anken. Vedtaksinstansen skal iverksette det som er gitt medhold i. Den delen det ikke gis medhold i sendes til Trygderetten.","JA (delvis)","JA (delvis)","","","","",anke_avsluttet_json("DELVIS_MEDHOLD")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser",
        "Klageinstans har sendt anken til Trygderetten med innstilling om stadfestelse av eget vedtak.","NEI","NEI","","","","",anke_avsluttet_json("STADFESTELSE")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","AVVIST","avsluttet + journalpostReferanser",
        "Klageinstans har sendt anken til Trygderetten med innstilling om avvisning.","NEI","NEI","","","","",anke_avsluttet_json("AVVIST")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HEVET","avsluttet + journalpostReferanser",
        "Bruker har trukket anken direkte hos Trygderetten (ikke via Klageinstans), og Trygderetten returnerer hevet-kjennnelse. Vedtaksinstans skal ikke agere paa dette utfallet.","NEI","NEI","","","","",anke_avsluttet_json("HEVET")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HENVIST","avsluttet + journalpostReferanser",
        "Trygderetten sender saken tilbake til Klageinstans og ber om ytterligere behandling foer saken ev. returneres til Trygderetten igjen, eller det opprettes helt ny behandling.","NEI","NEI","","","","",anke_avsluttet_json("HENVIST")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser",
        "Ankevedtak endret til ugunst for bruker.","JA","JA","","","","",anke_avsluttet_json("UGUNST")),
    row("ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser",
        "Saken henlegges – behandles ikke videre. Skiller seg fra Trukket ved at det ikke er bruker som har trukket saken. Typisk: den saken gjelder er doed og dodsboet tar ikke saken videre.","NEI","NEI","","","","",anke_avsluttet_json("HENLAGT")),

    [""] * 11,

    # ── ANKE I TRYGDERETTEN ──────────────────────────────────────────────────────
    # Ankesak sendt fra Klageinstans til Trygderetten.
    # OPPRETTET-eventet signaliserer at saken er oversendt og angir Klageinstansens innstilling.
    # Trygderettens endelige avgjoerelse returneres via ANKEBEHANDLING_AVSLUTTET (se over).
    ["=== ANKE I TRYGDERETTEN ==="] + [""] * 11,
    row("ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","(utfall ikke satt)","sendtTilTrygderetten (dato)",
        "Ankesaken er oversendt Trygderetten. Ingen innstilling fra Klageinstans er registrert ennaa.","–","–","","","","",trygderetten_json("(utfall ikke satt)")),
    row("ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","DELVIS_MEDHOLD","sendtTilTrygderetten (dato)",
        "Ankesaken er oversendt Trygderetten. Klageinstans hadde gitt delvis medhold – den resterende delen sendes til Trygderetten.","–","–","","","","",trygderetten_json("DELVIS_MEDHOLD")),
    row("ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","INNSTILLING_STADFESTELSE","sendtTilTrygderetten (dato)",
        "Ankesaken er oversendt Trygderetten med Klageinstansens innstilling: stadfestelse av eget vedtak.","–","–","","","","",trygderetten_json("INNSTILLING_STADFESTELSE")),
    row("ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","INNSTILLING_AVVIST","sendtTilTrygderetten (dato)",
        "Ankesaken er oversendt Trygderetten med Klageinstansens innstilling: avvisning av anken.","–","–","","","","",trygderetten_json("INNSTILLING_AVVIST")),

    [""] * 11,

    # ── OMGJOERINGSKRAV (Klageinstans) ───────────────────────────────────────────
    # Omgjoeringskrav: bruker krever at Klageinstans omgjoer et av sine tidligere vedtak,
    # utenfor ordinaer klage- og ankebehandling.
    ["=== OMGJOERINGSKRAV (Klageinstans) ==="] + [""] * 11,
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser",
        "Klageinstans har registrert omgjoringskravet som trukket av bruker.","NEI","NEI","","","","",omgjoering_json("TRUKKET")),
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","BESLUTNING_OM_IKKE_AA_OMGJOERE","avsluttet + journalpostReferanser",
        "Klageinstans beslutter aa ikke omgjoere eget vedtak.","NEI","NEI","","","","",omgjoering_json("BESLUTNING_OM_IKKE_AA_OMGJOERE")),
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","MEDHOLD_ETTER_FVL_35","avsluttet + journalpostReferanser",
        "Klageinstans har gitt medhold i omgjoringskravet etter forvaltningsloven § 35 (medhold utenfor ordinaer klage- og ankebehandling). Vedtaksinstans skal iverksette.","JA","JA","","","","",omgjoering_json("MEDHOLD_ETTER_FVL_35")),
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","STADFESTET_MED_ANNEN_BEGRUNNELSE","avsluttet + journalpostReferanser",
        "Klageinstans opprettholder eget tidligere vedtak, men med en saa vesensforskjellig begrunnelse at det fattes nytt vedtak med annen begrunnelse.","JA","NEI","","","","",omgjoering_json("STADFESTET_MED_ANNEN_BEGRUNNELSE")),
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser",
        "Omgjoringskravet er avslaatt og vedtaket endret til brukers ugunst.","JA","JA","","","","",omgjoering_json("UGUNST")),
    row("OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser",
        "Saken henlegges – behandles ikke videre. Typisk: den saken gjelder er doed og dodsboet tar ikke saken videre.","NEI","NEI","","","","",omgjoering_json("HENLAGT")),

    [""] * 11,

    # ── BEGJAEERING OM GJENOPPTAK (Klageinstans) ─────────────────────────────────
    # Bruker har sendt inn begjaeering om at Trygderettens kjennnelse i en tidligere ankesak
    # blir gjenopptatt. Begjaeringen fremsettes for Klageinstans.
    ["=== BEGJAEERING OM GJENOPPTAK (Klageinstans) ==="] + [""] * 11,
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser",
        "Klageinstans har registrert begjaeringen om gjenopptak som trukket av bruker. Sendes ikke til Trygderetten.","NEI","NEI","","","","",gjenopptak_json("TRUKKET")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","MEDHOLD_ETTER_FVL_35","avsluttet + journalpostReferanser",
        "Klageinstans har gitt medhold etter forvaltningsloven § 35. Vedtaksinstans skal iverksette. Sendes ikke til Trygderetten.","JA","JA","","","","",gjenopptak_json("MEDHOLD_ETTER_FVL_35")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","INNSTILLING_GJENOPPTAS_IKKE","avsluttet + journalpostReferanser",
        "Klageinstans har oversendt begjaeringen til Trygderetten med innstilling om at saken ikke gjenopptas.","NEI","NEI","","","","",gjenopptak_json("INNSTILLING_GJENOPPTAS_IKKE")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","INNSTILLING_GJENOPPTAS_MEN_STADFESTET","avsluttet + journalpostReferanser",
        "Klageinstans har oversendt begjaeringen til Trygderetten med innstilling om at saken gjenopptas, men at resultatet likevel boer vaere stadfestelse av Klageinstansens tidligere vedtak.","NEI","NEI","","","","",gjenopptak_json("INNSTILLING_GJENOPPTAS_MEN_STADFESTET")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","INNSTILLING_AVVIST","avsluttet + journalpostReferanser",
        "Klageinstans har oversendt begjaeringen til Trygderetten med innstilling om avvisning (frist oversittet eller andre formkrav ikke oppfylt).","NEI","NEI","","","","",gjenopptak_json("INNSTILLING_AVVIST")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser",
        "Saken henlegges – behandles ikke videre. Typisk: den saken gjelder er doed og dodsboet tar ikke saken videre.","NEI","NEI","","","","",gjenopptak_json("HENLAGT")),

    [""] * 11,

    # ── BEGJAEERING OM GJENOPPTAK I TRYGDERETTEN ─────────────────────────────────
    # Trygderetten behandler begjaeringen om gjenopptak av en av sine egne kjennnelser.
    # Kabal registrerer utfallet fra Trygderetten naaer kjennelsen mottas.
    ["=== BEGJAEERING OM GJENOPPTAK I TRYGDERETTEN ==="] + [""] * 11,
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD","avsluttet + journalpostReferanser",
        "Trygderetten har gjenopptatt saken og resultatet er fullt eller delvis medhold. Vedtaksinstans skal iverksette.","–","JA","","","","",gjenopptak_json("GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","GJENOPPTATT_OPPHEVET","avsluttet + journalpostReferanser",
        "Trygderetten har gjenopptatt saken og resultatet er opphevelse. Vanligvis vil Klageinstans selv gjoere ny behandling. Noen ganger sendes saken tilbake til vedtaksenheten for ny utredning og nytt vedtak.","–","NEI","","","","",gjenopptak_json("GJENOPPTATT_OPPHEVET")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","GJENOPPTATT_STADFESTET","avsluttet + journalpostReferanser",
        "Trygderetten har gjenopptatt saken, men resultatet er likevel stadfestelse av Klageinstansens tidligere vedtak.","–","NEI","","","","",gjenopptak_json("GJENOPPTATT_STADFESTET")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","IKKE_GJENOPPTATT","avsluttet + journalpostReferanser",
        "Trygderetten har kommet til at den tidligere ankebehandlingen ikke skal gjenopptas.","–","NEI","","","","",gjenopptak_json("IKKE_GJENOPPTATT")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","AVVIST","avsluttet + journalpostReferanser",
        "Trygderetten har avvist begjaeringen om gjenopptak.","–","NEI","","","","",gjenopptak_json("AVVIST")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","HEVET","avsluttet + journalpostReferanser",
        "Parten har trukket begjaeringen direkte hos Trygderetten. Klageinstans faar hevet-kjennnelse. Vedtaksinstans skal ikke agere paa dette utfallet.","-","NEI","","","","",gjenopptak_json("HEVET")),
    row("GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser",
        "Gjenopptaksbehandlingen ga utfall til brukers ugunst.","–","JA","","","","",gjenopptak_json("UGUNST")),

    [""] * 11,

    # ── NY BEHANDLING ETTER TRYGDERETTEN HAR OPPHEVET ────────────────────────────
    # Trygderetten har opphevet enten en anke eller begjaeering om gjenopptak,
    # og det er Klageinstans som gjoer ny behandling.
    # Mulige utfall er de samme som ved klagebehandling.
    ["=== NY BEHANDLING ETTER TRYGDERETTEN HAR OPPHEVET (Klageinstans) ==="] + [""] * 11,
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","TRUKKET","avsluttet + journalpostReferanser",
        "Klageinstans har registrert saken som trukket av bruker.","NEI","NEI","","","","",etter_trygderetten_json("TRUKKET")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","RETUR","avsluttet + journalpostReferanser",
        "Klageinstans returnerer saken uten avgjorelse pga. formelle feil (jfr. fvl. § 33). Feil maa rettes og saken sendes til Klageinstans paa nytt.","NEI","NEI","","","","",etter_trygderetten_json("RETUR")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","OPPHEVET","avsluttet + journalpostReferanser",
        "Klageinstans kan ikke vurdere saken – ikke godt nok utredet. Vedtaksenheten skal utrede og gjoere nytt vedtak. Saken skal ikke tilbake til Klageinstans med mindre bruker klager paa nytt vedtak.","NEI","NEI","","","","",etter_trygderetten_json("OPPHEVET")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt medhold. Vedtaksinstans skal iverksette.","JA","JA","","","","",etter_trygderetten_json("MEDHOLD")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser",
        "Klageinstans har gitt delvis medhold. Vedtaksinstans skal iverksette.","JA","JA","","","","",etter_trygderetten_json("DELVIS_MEDHOLD")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser",
        "Klageinstans har stadfestet vedtaksinstansens vedtak.","JA","NEI","","","","",etter_trygderetten_json("STADFESTELSE")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","UGUNST","avsluttet + journalpostReferanser",
        "Vedtaksinstansens vedtak var ugyldig og er gjort om til brukers ugunst (Ugyldig).","JA","JA","","","","",etter_trygderetten_json("UGUNST")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","AVVIST","avsluttet + journalpostReferanser",
        "Klageinstans har avvist saken. Typisk pga. oversittet frist eller formfeil.","JA","NEI","","","","",etter_trygderetten_json("AVVIST")),
    row("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","HENLAGT","avsluttet + journalpostReferanser",
        "Saken henlegges – behandles ikke videre. Typisk: den saken gjelder er doed og dodsboet tar ikke saken videre.","NEI","NEI","","","","",etter_trygderetten_json("HENLAGT")),

    [""] * 11,

    # ── FEILREGISTRERT ───────────────────────────────────────────────────────────
    ["=== FEILREGISTRERT ==="] + [""] * 11,
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=KLAGE","feilregistrert (dato) + navIdent + reason",
        "En klagebehandling ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("KLAGE")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=ANKE","feilregistrert (dato) + navIdent + reason",
        "En ankebehandling ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("ANKE")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=ANKE_I_TRYGDERETTEN","feilregistrert (dato) + navIdent + reason",
        "En anke-i-Trygderetten-behandling ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("ANKE_I_TRYGDERETTEN")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET","feilregistrert (dato) + navIdent + reason",
        "En behandling etter Trygderetten opphevet ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=OMGJOERINGSKRAV","feilregistrert (dato) + navIdent + reason",
        "En omgjoringskravbehandling ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("OMGJOERINGSKRAV")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEGJAERING_OM_GJENOPPTAK","feilregistrert (dato) + navIdent + reason",
        "En begjaeering om gjenopptak ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("BEGJAERING_OM_GJENOPPTAK")),
    row("BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN","feilregistrert (dato) + navIdent + reason",
        "En begjaeering om gjenopptak i Trygderetten ble feilregistrert i Kabal. Behandlingen annulleres.","–","NEI","","","","",feilregistrert_json("BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN")),
]

with open("/Users/Rituvesh.Kumar/src/historisk-super/docs/kabal-behandling-utfall-aksjonstabell.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerows(rows)

print(f"Done: {len(rows)} rows written")

