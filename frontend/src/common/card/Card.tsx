import { Box, type BoxProps, Heading, VStack } from "@navikt/ds-react";

type CardProps = BoxProps & {
    title?: string;
};

export function Card(props: CardProps) {
    const { title, children } = props;
    return (
        <Box
            padding="space-24"
            background="neutral-soft"
            borderWidth="1"
            borderRadius="8"
            borderColor="neutral-subtle"
            {...props}
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
