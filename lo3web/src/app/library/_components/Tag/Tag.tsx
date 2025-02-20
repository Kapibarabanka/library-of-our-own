import styles from './tag.module.css';

export default function Tag({
    label,
    withCross = false,
    onTagClicked,
}: {
    label: string;
    withCross?: boolean;
    onTagClicked: () => void;
}) {
    return (
        <li className={styles.wrap}>
            <span className={styles.text} onClick={() => onTagClicked()}>
                {label + (withCross ? ' X' : '')}
            </span>
        </li>
    );
}
