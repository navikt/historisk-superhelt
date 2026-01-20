import React, {Component, ErrorInfo, ReactNode} from 'react';
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {ProblemDetail} from "@generated";


interface Props {
    children: ReactNode;
    fallback?: (error: Error, problemDetails?: ProblemDetail) => ReactNode;
}

interface State {
    hasError: boolean;
    error?: Error;
}

export class RfcErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {hasError: false};
    }

    static getDerivedStateFromError(error: Error): State {

        return {
            hasError: true,
            error,
        };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('RFC9457ErrorBoundary caught an error:', error, errorInfo);
    }

    private renderDefaultFallback() {
        const {error,} = this.state;
        return <ErrorAlert error={error}/>;
    }

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback(this.state.error!);
            }
            return this.renderDefaultFallback();
        }

        return this.props.children;
    }
}
