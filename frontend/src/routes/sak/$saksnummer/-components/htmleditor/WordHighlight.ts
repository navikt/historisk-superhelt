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
            if (span.style.backgroundColor !== "") {
                const mark = doc.createElement("mark");
                mark.append(...Array.from(span.childNodes));
                span.replaceWith(mark);
            }
        });
        return doc.body.innerHTML;
    },
});
