import React, {Component, ErrorInfo, ReactNode} from 'react';
import {ErrorAlert} from "~/components/error/ErrorAlert";
import {ProblemDetail} from "~/api";


interface Props {
  children: ReactNode;
  fallback?: (error: Error, problemDetails?: ProblemDetail) => ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
  problemDetails?: ProblemDetail;
}

export class RfcErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // Check if error contains RFC 9457 problem details
    let problemDetails: ProblemDetail | undefined;

    try {
        // Attempt to parse error as ProblemDetail
      if ((error as any).type && (error as any).title && (error as any).status){
        problemDetails = error as ProblemDetail
      }


    } catch {
      // Not a valid RFC 9457 format, continue with standard error handling
    }

    return {
      hasError: true,
      error,
      problemDetails
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('RFC9457ErrorBoundary caught an error:', error, errorInfo);
  }

  private renderDefaultFallback() {
    const { error, problemDetails } = this.state;
    return <ErrorAlert problemDetails={problemDetails} error={error}/>;
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback(this.state.error!, this.state.problemDetails);
      }
      return this.renderDefaultFallback();
    }

    return this.props.children;
  }
}
