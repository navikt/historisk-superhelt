interface FixedHtmlProps {
    html: string;
}

export function FixedHtml({html}: FixedHtmlProps) {
    return (
        <div>
            {/** biome-ignore lint/security/noDangerouslySetInnerHtml: html er sjekket fra før */}
            <div dangerouslySetInnerHTML={{__html: html}}/>
        </div>
    )
}