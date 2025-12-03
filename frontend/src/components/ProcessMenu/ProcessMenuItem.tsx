import React from 'react';

import {BodyShort, Box, Tooltip} from '@navikt/ds-react';

import {StepIcon} from './StepIcon';
import {StepType} from './StepType';

import styles from './step.module.css';
import {Link, LinkProps} from "@tanstack/react-router";

interface ProcessButtonProps extends LinkProps {
    label: string;
    stepType?: StepType;
}

export const ProcessMenuItem = (props: ProcessButtonProps) => {
    const {
        label,
        stepType = StepType.default,
            ...linkProps
    } = props;

    function renderButton(isActive: boolean) {


        const stepIndicatorCls = `${styles.step__button} ${styles[stepType]} ${isActive ? styles['active'] : ''} `

        return (<>
                <Tooltip content={label} placement="bottom">

                    <button className={stepIndicatorCls}>
                        <StepIcon type={stepType} usePartialStatus={false}/>
                        <BodyShort as="span" size="small" className={styles.step__text}>
                            {label}
                        </BodyShort>
                    </button>

                </Tooltip>
                {isActive && <div className={`${styles['step__arrow-container']}`}/>}
            </>
        );
    }

    return <Box className={styles.step}>
        <Link {...linkProps} >
            {({isActive}) => {
                return renderButton(isActive);
            }}
        </Link>
    </Box>
};
