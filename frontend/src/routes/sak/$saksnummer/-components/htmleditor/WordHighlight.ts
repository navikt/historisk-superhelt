import { Extension } from "@tiptap/core";

/* 
markerte elementer i Word har ofte inline-styles for bakgrunnsfarge, 
som vi ønsker å konvertere til <mark> for å beholde markeringen når det limes inn i editoren
*/
export const WordHighlight = Extension.create({
    name: "wordHighlight",
    transformPastedHTML(html: string) {
        const doc = new DOMParser().parseFromString(html, "text/html");
        doc.querySelectorAll<HTMLElement>("span[style]").forEach((span) => {
            const rawStyle = span.getAttribute("style") ?? "";
            const isHighlighted =
                span.style.backgroundColor !== "" ||
                /mso-highlight\s*:\s*(?!none\b|initial\b|inherit\b)/i.test(rawStyle);
            if (isHighlighted) {
                const mark = doc.createElement("mark");
                mark.append(...Array.from(span.childNodes));
                span.replaceWith(mark);
            }
        });
        return doc.body.innerHTML;
    },
});

document.addEventListener("paste", (e) => {
    const html = e.clipboardData!.getData("text/html");
    console.log(html);
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, "text/html");
    doc.querySelectorAll("[style]").forEach((el) => {
        const s = el.getAttribute("style");
        if (s && (s.includes("background") || s.includes("highlight") || s.includes("mso-"))) {
            console.log("HIGHLIGHT CANDIDATE:", s);
        }
    });
})