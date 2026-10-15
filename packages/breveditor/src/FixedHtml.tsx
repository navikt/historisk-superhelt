import DOMPurify from "dompurify";

interface FixedHtmlProps {
    html: string;
}

export function FixedHtml({ html }: FixedHtmlProps) {
    const sanitizedHtml = DOMPurify.sanitize(html);
    return (
        <div>
            <div className="brev-innhold" dangerouslySetInnerHTML={{ __html: sanitizedHtml }} />
        </div>
    );
}
