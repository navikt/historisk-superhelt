import {Alert} from "@navikt/ds-react";
import React from "react";
import {ProblemDetail} from "@api";

export function ErrorAlert(props: {
    problemDetails?: ProblemDetail,
    error?: Error
}) {
    return <div>
        <Alert variant="error">
            <strong>{props.problemDetails?.title || "Noe gikk galt"}</strong>
            {props.problemDetails?.detail && <p>{props.problemDetails.detail}</p>}
            {!props.problemDetails?.detail && props.error?.message && <p>{props.error.message}</p>}
        </Alert>
    </div>;
}