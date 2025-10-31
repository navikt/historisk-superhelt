import {Alert} from "@navikt/ds-react";
import React from "react";
import {ProblemDetail} from "@api";

type ErrorAlertType = Error | ProblemDetail;

interface ErrorAlertProps {
    error: ErrorAlertType | undefined;
}

export function ErrorAlert({error}: ErrorAlertProps) {
    if (!error) {
        return null;
    }
    const isProblemDetail = (err: ErrorAlertType): err is ProblemDetail =>
        'type' in err && 'title' in err && 'status' in err;

    const problemDetails = isProblemDetail(error);

    return (
        <Alert variant="error">
            <strong>{problemDetails ? error.title : "Noe gikk galt"}</strong>
            <p>{problemDetails ? error.detail : error.message}</p>
        </Alert>
    );
}