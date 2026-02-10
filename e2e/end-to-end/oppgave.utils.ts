import {request} from "@playwright/test";
import {v4 as uuidv4} from 'uuid';
import {format} from 'date-fns';


const mockBaseURL = "http://localhost:9080";

export class OppgaveUtils {
    async opprettJFR(personIdent: string): Promise<number> {
        const context = await request.newContext({
            baseURL: mockBaseURL,
        });
        const response = await context.post("/oppgave-mock/api/v1/oppgaver", {
            data: {
                personident: personIdent,
                tema: "HEL",
                behandlingstema: "ab0129",
                oppgavetype: "JFR",
                aktivDato: format(new Date(), 'yyyy-MM-dd'),
                fristFerdigstillelse: format(new Date(), 'yyyy-MM-dd'),
                prioritet: "NORM",
                journalpostId: uuidv4(),
            },
        });

        const oppgave = await response.json();
        console.debug(`Opprettet oppgave med id ${oppgave.id} for ${personIdent} `);
        await context.dispose();
        return oppgave.id;
    }

    async tildelOppgave(oppgaveId: number, saksbehandler: string) {
        console.debug(`Tildeler oppgave ${oppgaveId} til ${saksbehandler} `);
        const context = await request.newContext({
            baseURL: mockBaseURL,
        });
        await context.patch(`/oppgave-mock/api/v1/oppgaver/${oppgaveId}`, {
            data: {
                versjon: 1,
                tilordnetRessurs: saksbehandler,
            },
        });
        await context.dispose();
    }
}
