import { Editor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { act, useState } from "react";
import { describe, expect, test } from "vitest";
import { page, userEvent } from "vitest/browser";
import { render } from "vitest-browser-react";
import type { Standardtekst, StandardtekstResponse } from "./Standardtekst.types";
import { StandardtekstExtension } from "./StandardtekstExtension";
import { StandardtekstModal } from "./StandardtekstModal";

describe("StandardtekstModal", () => {
    function response(
        data: Standardtekst[] | undefined,
        status: Partial<StandardtekstResponse> = {},
    ): StandardtekstResponse {
        return {
            data,
            isPending: false,
            isError: false,
            ...status,
        };
    }

    function createEditor() {
        return new Editor({
            element: document.createElement("div"),
            extensions: [StarterKit, StandardtekstExtension],
            content: "<p>Redigerbar tekst</p>",
        });
    }

    test("filtrerer lokalt og viser tittel, tags og forhåndsvisning", async () => {
        const catalog: Standardtekst[] = [
            {
                id: "general",
                title: "Utbetaling",
                html: "<p>Generell tekst</p>",
                tags: ["betaling"],
            },
            {
                id: "parykk",
                title: "Innvilgelse",
                html: "<p>Hodeform</p>",
                tags: ["Parykk", "avvikende hodeform"],
                stonadstype: "Parykk/hodeplagg",
            },
            {
                id: "bryst",
                title: "Brystprotese",
                html: "<p>Støtte til brystprotese</p>",
                tags: ["Brystprotese"],
                stonadstype: "Brystprotese/spesial-bh",
            },
        ];
        const editor = createEditor();
        const screen = await render(<StandardtekstModal editor={editor} standardtekster={response(catalog)} />);

        try {
            editor.commands.openStandardtekstModal();
            await expect.element(page.getByRole("button", { name: "Utbetaling" })).toBeVisible();
            await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
            await expect.element(page.getByRole("button", { name: "Brystprotese" })).toBeVisible();

            await userEvent.click(page.getByRole("button", { name: /Innvilgelse Parykk/ }));
            await expect.element(page.getByText("avvikende hodeform", { exact: true })).toBeVisible();
            await expect
                .element(page.getByText("Parykk/hodeplagg", { exact: true }))
                .toHaveAttribute("data-color", "meta-purple");
            await expect.element(page.getByText("Parykk", { exact: true })).toHaveAttribute("data-color", "neutral");
            await expect.element(page.getByText("Hodeform", { exact: true })).toBeVisible();
            await expect
                .element(
                    page.getByRole("button", { name: "Utbetaling" }).getByText("Parykk/hodeplagg", { exact: true }),
                )
                .not.toBeInTheDocument();

            const search = page.getByRole("searchbox", { name: "Søk etter standardtekst" });
            await userEvent.type(search, "hodeform");
            await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
            await expect.element(page.getByRole("button", { name: "Utbetaling" })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Brystprotese" })).not.toBeInTheDocument();

            await userEvent.clear(search);
            await userEvent.type(search, "utbetaling");
            await expect.element(page.getByRole("button", { name: "Utbetaling" })).toBeVisible();
            await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).not.toBeInTheDocument();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });

    test("viser tomtilstand for en tilgjengelig, tom katalog", async () => {
        const editor = createEditor();
        const screen = await render(<StandardtekstModal editor={editor} standardtekster={response([])} />);

        try {
            editor.commands.openStandardtekstModal();

            await expect.element(page.getByText("Ingen standardtekster funnet.")).toBeVisible();
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });

    test("viser data når en ventende respons oppdateres", async () => {
        const standardtekst: Standardtekst = {
            id: "innvilgelse",
            title: "Innvilgelse",
            html: "<p>Innhold</p>",
            tags: [],
        };
        const editor = createEditor();
        let updateResponse: (nextResponse: StandardtekstResponse) => void = () => {};

        function ModalWithUpdatedResponse() {
            const [responseState, setResponseState] = useState(response([standardtekst], { isPending: true }));
            updateResponse = setResponseState;
            return <StandardtekstModal editor={editor} standardtekster={responseState} />;
        }

        const screen = await render(<ModalWithUpdatedResponse />);

        try {
            editor.commands.openStandardtekstModal();
            await expect.element(page.getByRole("status")).toHaveTextContent("Søker etter standardtekster");
            await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();

            await act(async () => updateResponse(response([standardtekst])));
            await expect.element(page.getByRole("button", { name: standardtekst.title })).toBeVisible();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });

    test("viser generell feil uten rå feilmelding eller prøv igjen", async () => {
        const standardtekst: Standardtekst = {
            id: "utdatert",
            title: "Skal ikke vises",
            html: "<p>Utdatert</p>",
            tags: [],
        };
        const editor = createEditor();
        const screen = await render(
            <StandardtekstModal
                editor={editor}
                standardtekster={response([standardtekst], {
                    isError: true,
                    error: new Error("intern feildetalj"),
                })}
            />,
        );

        try {
            editor.commands.openStandardtekstModal();

            await expect.element(page.getByText("Standardtekstene kunne ikke lastes inn.")).toBeVisible();
            await expect.element(page.getByText("intern feildetalj")).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Prøv igjen" })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });

    test("fjerner valgt tekst når oppdatert respons ikke inneholder den lenger", async () => {
        const standardtekst: Standardtekst = {
            id: "utdatert",
            title: "Innvilgelse",
            html: "<p>Skal ikke settes inn</p>",
            tags: [],
        };
        const editor = createEditor();
        let updateResponse: (nextResponse: StandardtekstResponse) => void = () => {};

        function ModalWithUpdatedResponse() {
            const [responseState, setResponseState] = useState(response([standardtekst]));
            updateResponse = setResponseState;
            return <StandardtekstModal editor={editor} standardtekster={responseState} />;
        }

        const screen = await render(<ModalWithUpdatedResponse />);

        try {
            editor.commands.openStandardtekstModal();
            await userEvent.click(page.getByRole("button", { name: standardtekst.title }));
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeEnabled();

            await act(async () => updateResponse(response([])));

            await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });

    test("fjerner valgt tekst når søket skjuler den", async () => {
        const standardtekst: Standardtekst = {
            id: "parykk",
            title: "Parykk",
            html: "<p>Stønad</p>",
            tags: ["Innvilgelse"],
            stonadstype: "Parykk/hodeplagg",
        };
        const editor = createEditor();
        const screen = await render(<StandardtekstModal editor={editor} standardtekster={response([standardtekst])} />);

        try {
            editor.commands.openStandardtekstModal();
            await userEvent.click(page.getByRole("button", { name: standardtekst.title }));
            await userEvent.type(page.getByRole("searchbox", { name: "Søk etter standardtekst" }), "mangler");

            await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
            await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
        } finally {
            await screen.unmount();
            editor.destroy();
        }
    });
});
