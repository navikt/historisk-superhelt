import {ReactNode, useState} from 'react';
import {ChevronDownIcon, ChevronUpIcon} from '@navikt/aksel-icons';
import styles from './BrevExpandable.module.css';

interface AccordionProps {
    title: string;
    children: ReactNode;
    defaultExpanded?: boolean;
}

export function BrevExpandable({ title, children, defaultExpanded = false }: AccordionProps) {
    const [isExpanded, setIsExpanded] = useState(defaultExpanded);

    return (
        <div className={styles.accordion}>
            <button
                type="button"
                className={styles.accordionButton}
                onClick={() => setIsExpanded(!isExpanded)}
                aria-expanded={isExpanded}
            >
                <span>{title}</span>
                {isExpanded ? <ChevronUpIcon /> : <ChevronDownIcon />}
            </button>
            {isExpanded && (
                <div className={styles.accordionContent}>
                    {children}
                </div>
            )}
        </div>
    );
}
