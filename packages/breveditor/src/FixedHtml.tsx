import DOMPurify from "dompurify";

interface FixedHtmlProps {
    html: string;
}

export function FixedHtml({ html }: FixedHtmlProps) {
    const sanitizedHtml = DOMPurify.sanitize(html);
    return (
        <div>
            <div dangerouslySetInnerHTML={{ __html: sanitizedHtml }} />
        </div>
    );
}
