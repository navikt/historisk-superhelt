import {HStack} from '@navikt/ds-react';

interface ProcessMenuProps {
    children?: React.ReactNode;
}

export const ProcessMenu = ({children}: ProcessMenuProps) => (
    <HStack gap="space-16">
        {children}
    </HStack>
);