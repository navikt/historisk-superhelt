import { Box, Heading, VStack } from "@navikt/ds-react";

interface CardProps {
    title?: string;
    children: React.ReactNode;
    className?: string;
}

export function Card({ title, children, className }: CardProps) {
    return (
        <Box
            className={className}
            padding="space-24"
            background="neutral-soft"
            borderWidth="1"
            borderRadius="8"
            borderColor="neutral-subtle"
            asChild
        >
            <VStack gap="space-16">
                {title && (
                    <Heading textColor="subtle" size="xsmall" level="3">
                        {title}
                    </Heading>
                )}
                {children}
            </VStack>
        </Box>
    );
}
