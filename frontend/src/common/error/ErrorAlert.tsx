import type { ProblemDetail } from "@generated";
import { LocalAlert } from "@navikt/ds-react";

type ErrorAlertType = Error | ProblemDetail;

interface ErrorAlertProps {
    error: ErrorAlertType | undefined | null;
}

export function ErrorAlert({ error }: ErrorAlertProps) {
    if (!error) {
        return null;
    }
    const isProblemDetail = (err: ErrorAlertType): err is ProblemDetail =>
        "detail" in err && "title" in err && "status" in err;

    const problemDetails = isProblemDetail(error);

    return (
        <LocalAlert status="error">
            <LocalAlert.Header>
                <LocalAlert.Title>{problemDetails ? error.title : "Noe gikk galt"}</LocalAlert.Title>
            </LocalAlert.Header>
            <LocalAlert.Content>{problemDetails ? error.detail : error.message}</LocalAlert.Content>
        </LocalAlert>
    );
}
