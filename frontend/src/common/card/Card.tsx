import {InfoCard, VStack} from "@navikt/ds-react";

interface CardProps {
    title: string;
    children: React.ReactNode;
}

export function Card(props: CardProps) {
    return (
        <InfoCard data-color="neutral" as="section">
            <InfoCard.Header>
                <InfoCard.Title>{props.title}</InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
                <VStack gap="space-24">
                    {props.children}
                </VStack>
            </InfoCard.Content>
        </InfoCard>
    );
}