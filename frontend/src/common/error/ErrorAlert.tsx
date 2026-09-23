import type { ProblemDetail } from "@generated";
import { LocalAlert } from "@navikt/ds-react";

type ErrorAlertType = Error | ProblemDetail;

interface ErrorAlertProps {
    error: ErrorAlertType | undefined | null | unknown;
}

export function ErrorAlert({ error }: ErrorAlertProps) {
    if (!error) {
        return null;
    }
    const isProblemDetail = (err: ErrorAlertType): err is ProblemDetail =>
        "detail" in err && "title" in err && "status" in err;

    const problemDetails = isProblemDetail(error);

    const title = problemDetails ? error.title : "Noe gikk galt";
    const description = problemDetails ? error.detail : error instanceof Error ? error.message : "Ukjent feil";

    return (
        <LocalAlert status="error">
            <LocalAlert.Header>
                <LocalAlert.Title>{title}</LocalAlert.Title>
            </LocalAlert.Header>
            <LocalAlert.Content>{description}</LocalAlert.Content>
        </LocalAlert>
    );
}
