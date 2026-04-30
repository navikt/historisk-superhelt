import { Box, type BoxProps, Heading, VStack } from "@navikt/ds-react";

type CardProps = BoxProps & {
    tittel?: string;
};

export function Card(props: CardProps) {
    const { tittel, children, ...boxProps } = props;
    return (
        <Box
            padding="space-24"
            background="neutral-soft"
            borderWidth="1"
            borderRadius="8"
            borderColor="neutral-subtle"
            {...boxProps}
        >
            <VStack gap="space-16">
                {tittel && (
                    <Heading textColor="subtle" size="xsmall" level="3">
                        {tittel}
                    </Heading>
                )}
                {children}
            </VStack>
        </Box>
    );
}
