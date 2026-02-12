import { BodyShort, HStack, Link as AkselLink } from "@navikt/ds-react";
import { StepIcon } from "./StepIcon";
import { StepType } from "./StepType";
import { Link, type LinkProps, useMatches } from "@tanstack/react-router";
import styles from "./step.module.css";

interface ProcessMenuProps {
	children?: React.ReactNode;
}

interface ProcessButtonProps extends LinkProps {
	label: string;
	stepType?: StepType;
	disabled?: boolean;
}

const ProcessMenu = ({ children }: ProcessMenuProps) => (
	<HStack gap="space-16">{children}</HStack>
);

const ProcessMenuItem = ({
	label,
	stepType = StepType.default,
	disabled = false,
	...linkProps
}: ProcessButtonProps) => {
	const matches = useMatches({ select: (match) => match.at(-1) }); // Siste match er den mest spesifikke routen
	const isActive = matches?.fullPath === linkProps.to;

	return (
		<AkselLink
			as={Link}
			to={linkProps.to}
			className={styles.step__link}
			disabled={disabled}
		>
			<HStack
				align="center"
				justify="center"
				gap="space-4"
				className={`${styles.step} ${styles[stepType]} ${isActive && styles["active"]} ${disabled && styles["disabled"]}`}
			>
				<StepIcon type={stepType} usePartialStatus={false} />
				<BodyShort
					size="small"
					className={styles.step__text}
					weight={isActive ? "semibold" : "regular"}
					align="center"
					truncate
				>
					{label}
				</BodyShort>
			</HStack>
		</AkselLink>
	);
};

ProcessMenu.Item = ProcessMenuItem;

export { ProcessMenu };
