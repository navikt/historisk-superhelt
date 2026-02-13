import { BodyShort, HStack, Link as AkselLink } from "@navikt/ds-react";
import { StepIcon } from "./StepIcon";
import { StepType } from "./StepType";
import { type LinkProps, createLink } from "@tanstack/react-router";
import styles from "./step.module.css";

const Link = createLink(AkselLink);

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
	return (
		<Link
			to={linkProps.to}
			className={styles.step__link}
			disabled={disabled}
			aria-label={disabled ? `${label} (deaktivert)` : label}
			{...linkProps}
		>
			{({ isActive }) => {
				return (
					<HStack
						align="center"
						justify="center"
						gap="space-4"
						className={`${styles.step} ${styles[stepType]} ${isActive && styles["active"]} ${disabled && styles["disabled"]}`}
					>
						<StepIcon type={stepType} usePartialStatus={false} />
						<BodyShort
							size="small"
							weight={isActive ? "semibold" : "regular"}
							align="center"
							truncate
						>
							{label}
						</BodyShort>
					</HStack>
				);
			}}
		</Link>
	);
};

ProcessMenu.Item = ProcessMenuItem;

export { ProcessMenu };
