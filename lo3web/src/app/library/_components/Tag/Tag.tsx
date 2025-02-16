import styles from './tag.module.css';

export default function Tag({
    tag,
    withCross = false,
    onTagClicked,
}: {
    tag: string;
    withCross?: boolean;
    onTagClicked: (tag: string) => void;
}) {
    return (
        <li className={styles.wrap}>
            <span className={styles.text} onClick={() => onTagClicked(tag)}>
                {tag + (withCross ? ' X' : '')}
            </span>
        </li>
    );
}
