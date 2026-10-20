import { FilePdfIcon } from "@navikt/aksel-icons";
import { Button, Dialog, Tooltip } from "@navikt/ds-react";
import { useState } from "react";

interface PdfPreviewProps {
    previewUrl?: string;
    onBeforePreview: () => Promise<void>;
    readOnly?: boolean;
}

export function PdfPreview({ previewUrl, onBeforePreview, readOnly }: PdfPreviewProps) {
    const [open, setOpen] = useState(false);

    if (!previewUrl || readOnly) {
        return null;
    }

    const preview = async () => {
        await onBeforePreview();
        setOpen(true);
    };

    return (
        <>
            <Tooltip content="Forhåndsvis dokumentet som PDF" placement="top">
                <Button
                    type="button"
                    variant="tertiary"
                    data-color="neutral"
                    size="small"
                    aria-label="Forhåndsvis dokumentet som PDF"
                    onClick={preview}
                    icon={<FilePdfIcon aria-hidden="true" />}
                />
            </Tooltip>
            <Dialog open={open} onOpenChange={setOpen}>
                <Dialog.Popup width="large">
                    <Dialog.Header>
                        <Dialog.Title>Forhåndsvis dokumentet</Dialog.Title>
                    </Dialog.Header>
                    <Dialog.Body>
                        <embed
                            src={previewUrl}
                            type="application/pdf"
                            title="PDF-forhåndsvisning av brevet"
                            style={{ display: "block", width: "100%", height: "70vh", minHeight: "20rem" }}
                        />
                    </Dialog.Body>
                    <Dialog.Footer>
                        <Dialog.CloseTrigger>
                            <Button type="button" variant="secondary">
                                Lukk
                            </Button>
                        </Dialog.CloseTrigger>
                    </Dialog.Footer>
                </Dialog.Popup>
            </Dialog>
        </>
    );
}
