import {Box, Heading, VStack} from "@navikt/ds-react";

interface CardProps {
    title?: string;
    children: React.ReactNode;
}

export function Card({title, children}: CardProps) {

    return (
        <Box padding={"space-16"} background={"neutral-soft"}>
            <VStack gap="space-16">
                {title && <Heading textColor="subtle" size="xsmall">
                    {title}
                </Heading>}
                {children}
            </VStack>
        </Box>
    );
}
