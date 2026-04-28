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

rows = [
    ["Event-type","BehandlingDetaljer-felt","Utfall","Ekstra detaljer fra Kabal","Beskrivelse","Opprett oppgave? (JA/NEI/VURDER)","Oppgavetype hvis JA","Prioritet (HOY/NORMAL/LAV)","Kommentar (fagekspert fyller ut)","Eksempel JSON (fra Kabal)"],
    ["","","","","","","","","",""],
    ["=== KLAGEBEHANDLING ===","","","","","","","","",""],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","MEDHOLD","avsluttet + journalpostReferanser","Klager fikk medhold - vedtaket er omgjort. Ny behandling kan vaere noedvendig.","","","","",klage_json("MEDHOLD")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser","Klager fikk delvis medhold. Deler av vedtaket er omgjort.","","","","",klage_json("DELVIS_MEDHOLD")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","RETUR","avsluttet + journalpostReferanser","Saken er returnert til NAV-kontoret for ny behandling.","","","","",klage_json("RETUR")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","OPPHEVET","avsluttet + journalpostReferanser","Vedtaket er opphevet. Ny behandling paakrevd.","","","","",klage_json("OPPHEVET")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser","Vedtaket er stadfestet - klager fikk ikke medhold.","","","","",klage_json("STADFESTELSE")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser","Vedtaket endret til ugunst for klager.","","","","",klage_json("UGUNST")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","AVVIST","avsluttet + journalpostReferanser","Klagen er avvist (f.eks. for sent fremsatt).","","","","",klage_json("AVVIST")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser","Klager har trukket klagen.","","","","",klage_json("TRUKKET")],
    ["KLAGEBEHANDLING_AVSLUTTET","klagebehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser","Klagen er henlagt.","","","","",klage_json("HENLAGT")],
    ["","","","","","","","","",""],
    ["=== ANKEBEHANDLING ===","","","","","","","","",""],
    ["ANKEBEHANDLING_OPPRETTET","ankebehandlingOpprettet","(ingen utfall)","mottattKlageinstans (dato)","Anke er opprettet og mottatt i Kabal.","","","","",anke_opprettet_json()],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","MEDHOLD","avsluttet + journalpostReferanser","Anke fikk medhold.","","","","",anke_avsluttet_json("MEDHOLD")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser","Anke fikk delvis medhold.","","","","",anke_avsluttet_json("DELVIS_MEDHOLD")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","OPPHEVET","avsluttet + journalpostReferanser","Vedtaket er opphevet i ankeprosessen.","","","","",anke_avsluttet_json("OPPHEVET")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HEVET","avsluttet + journalpostReferanser","Ankesaken er hevet - sannsynligvis sendt til Trygderetten.","","","","",anke_avsluttet_json("HEVET")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HENVIST","avsluttet + journalpostReferanser","Saken er henvist til ny behandling.","","","","",anke_avsluttet_json("HENVIST")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser","Ankebeslutning stadfester opprinnelig vedtak.","","","","",anke_avsluttet_json("STADFESTELSE")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser","Ankevedtak endret til ugunst for bruker.","","","","",anke_avsluttet_json("UGUNST")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","AVVIST","avsluttet + journalpostReferanser","Anken er avvist.","","","","",anke_avsluttet_json("AVVIST")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","TRUKKET","avsluttet + journalpostReferanser","Anker er trukket.","","","","",anke_avsluttet_json("TRUKKET")],
    ["ANKEBEHANDLING_AVSLUTTET","ankebehandlingAvsluttet","HENLAGT","avsluttet + journalpostReferanser","Anken er henlagt.","","","","",anke_avsluttet_json("HENLAGT")],
    ["","","","","","","","","",""],
    ["=== ANKE I TRYGDERETTEN ===","","","","","","","","",""],
    ["ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","DELVIS_MEDHOLD","sendtTilTrygderetten (dato)","Anke sendt til Trygderetten - ankebehandling ga delvis medhold.","","","","",trygderetten_json("DELVIS_MEDHOLD")],
    ["ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","INNSTILLING_STADFESTELSE","sendtTilTrygderetten (dato)","Anke sendt til Trygderetten - innstilling er stadfestelse.","","","","",trygderetten_json("INNSTILLING_STADFESTELSE")],
    ["ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","INNSTILLING_AVVIST","sendtTilTrygderetten (dato)","Anke sendt til Trygderetten - innstilling er avvisning.","","","","",trygderetten_json("INNSTILLING_AVVIST")],
    ["ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET","ankeITrygderettenbehandlingOpprettet","(utfall ikke satt)","sendtTilTrygderetten (dato)","Anke sendt til Trygderetten - ingen utfall paa ankebehandlingen ennaa.","","","","",trygderetten_json("(utfall ikke satt)")],
    ["","","","","","","","","",""],
    ["=== BEHANDLING ETTER TRYGDERETTEN OPPHEVET ===","","","","","","","","",""],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","MEDHOLD","avsluttet + journalpostReferanser","Trygderetten opphevet - ny behandling ga medhold.","","","","",etter_trygderetten_json("MEDHOLD")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","DELVIS_MEDHOLD","avsluttet + journalpostReferanser","Trygderetten opphevet - ny behandling ga delvis medhold.","","","","",etter_trygderetten_json("DELVIS_MEDHOLD")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","RETUR","avsluttet + journalpostReferanser","Trygderetten opphevet - saken returnert til NAV.","","","","",etter_trygderetten_json("RETUR")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","OPPHEVET","avsluttet + journalpostReferanser","Trygderetten opphevet - vedtaket opphevet igjen.","","","","",etter_trygderetten_json("OPPHEVET")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","STADFESTELSE","avsluttet + journalpostReferanser","Trygderetten opphevet - vedtaket stadfestet paa nytt.","","","","",etter_trygderetten_json("STADFESTELSE")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","UGUNST","avsluttet + journalpostReferanser","Trygderetten opphevet - endret til ugunst.","","","","",etter_trygderetten_json("UGUNST")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","AVVIST","avsluttet + journalpostReferanser","Trygderetten opphevet - avvist.","","","","",etter_trygderetten_json("AVVIST")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","TRUKKET","avsluttet + journalpostReferanser","Trygderetten opphevet - trukket.","","","","",etter_trygderetten_json("TRUKKET")],
    ["BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET","behandlingEtterTrygderettenOpphevetAvsluttet","HENLAGT","avsluttet + journalpostReferanser","Trygderetten opphevet - henlagt.","","","","",etter_trygderetten_json("HENLAGT")],
    ["","","","","","","","","",""],
    ["=== OMGJOERINGSKRAV ===","","","","","","","","",""],
    ["OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","MEDHOLD_ETTER_FVL_35","avsluttet + journalpostReferanser","Omgjoeringskrav innvilget etter forvaltningsloven paragraf 35.","","","","",omgjoering_json("MEDHOLD_ETTER_FVL_35")],
    ["OMGJOERINGSKRAVBEHANDLING_AVSLUTTET","omgjoeringskravbehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser","Omgjoeringskrav avslaatt - endret til ugunst.","","","","",omgjoering_json("UGUNST")],
    ["","","","","","","","","",""],
    ["=== GJENOPPTAKSBEHANDLING ===","","","","","","","","",""],
    ["GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","MEDHOLD_ETTER_FVL_35","avsluttet + journalpostReferanser","Gjenopptak innvilget etter forvaltningsloven paragraf 35.","","","","",gjenopptak_json("MEDHOLD_ETTER_FVL_35")],
    ["GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD","avsluttet + journalpostReferanser","Sak gjenopptatt - delvis eller fullt medhold.","","","","",gjenopptak_json("GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD")],
    ["GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","GJENOPPTATT_OPPHEVET","avsluttet + journalpostReferanser","Sak gjenopptatt - vedtaket opphevet.","","","","",gjenopptak_json("GJENOPPTATT_OPPHEVET")],
    ["GJENOPPTAKSBEHANDLING_AVSLUTTET","gjenopptaksbehandlingAvsluttet","UGUNST","avsluttet + journalpostReferanser","Gjenopptaksbehandling ga ugunst.","","","","",gjenopptak_json("UGUNST")],
    ["","","","","","","","","",""],
    ["=== FEILREGISTRERT ===","","","","","","","","",""],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=KLAGE","feilregistrert (dato) + navIdent + reason","En klagebehandling ble feilregistrert i Kabal.","","","","",feilregistrert_json("KLAGE")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=ANKE","feilregistrert (dato) + navIdent + reason","En ankebehandling ble feilregistrert.","","","","",feilregistrert_json("ANKE")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=ANKE_I_TRYGDERETTEN","feilregistrert (dato) + navIdent + reason","Anke-i-Trygderetten-behandling feilregistrert.","","","","",feilregistrert_json("ANKE_I_TRYGDERETTEN")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET","feilregistrert (dato) + navIdent + reason","Behandling etter Trygderetten opphevet feilregistrert.","","","","",feilregistrert_json("BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=OMGJOERINGSKRAV","feilregistrert (dato) + navIdent + reason","Omgjoeringskravbehandling feilregistrert.","","","","",feilregistrert_json("OMGJOERINGSKRAV")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEGJAERING_OM_GJENOPPTAK","feilregistrert (dato) + navIdent + reason","Bejaering om gjenopptak feilregistrert.","","","","",feilregistrert_json("BEGJAERING_OM_GJENOPPTAK")],
    ["BEHANDLING_FEILREGISTRERT","behandlingFeilregistrert","type=BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN","feilregistrert (dato) + navIdent + reason","Bejaering om gjenopptak i Trygderetten feilregistrert.","","","","",feilregistrert_json("BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN")],
]

with open("/Users/Rituvesh.Kumar/src/historisk-super/docs/kabal-behandling-utfall-aksjonstabell.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerows(rows)

print(f"Done: {len(rows)} rows written")

