import {HStack} from '@navikt/ds-react';

interface ProcessMenuProps {
    children?: React.ReactNode;
}

export const ProcessMenu = ({children}: ProcessMenuProps) => (
    <HStack as="ol" justify="space-between" padding="0" align="end" gap="space-16">
        {children}
    </HStack>
);