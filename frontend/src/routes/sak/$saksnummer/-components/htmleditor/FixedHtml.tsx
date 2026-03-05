import DOMPurify from "dompurify";

interface FixedHtmlProps {
    html: string;
}

export function FixedHtml({ html }: FixedHtmlProps) {
    const sanitizedHtml = DOMPurify.sanitize(html);
    return (
        <div>
            {/** biome-ignore lint/security/noDangerouslySetInnerHtml: html er sjekket fra før */}
            <div dangerouslySetInnerHTML={{ __html: sanitizedHtml }} />
        </div>
    );
}
