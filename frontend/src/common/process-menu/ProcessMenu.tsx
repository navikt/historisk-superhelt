import { BodyShort, Box, HStack } from "@navikt/ds-react";
import { StepIcon } from "./StepIcon";
import { StepType } from "./StepType";
import { Link, type LinkProps } from "@tanstack/react-router";
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
	return (
		<Link {...linkProps} className={styles.step} disabled={disabled}>
			{({ isActive }) => {
				return (
					<Box
						className={`${styles.step__button} ${styles[stepType]} ${isActive ? styles["active"] : ""} ${disabled && styles["disabled"]}`}
					>
						<StepIcon type={stepType} usePartialStatus={false} />
						<BodyShort as="span" size="small" className={styles.step__text}>
							{label}
						</BodyShort>
						{isActive && (
							<div className={`${styles["step__arrow-container"]}`} />
						)}
					</Box>
				);
			}}
		</Link>
	);
};

ProcessMenu.Item = ProcessMenuItem;

export { ProcessMenu };