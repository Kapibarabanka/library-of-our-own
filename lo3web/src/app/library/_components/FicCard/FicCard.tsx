'use client';

import { FicCardData } from '@/types/domain-models';
import styles from './card.module.css';
import { getTagsByType } from '@/app/library/_utils/utils';
import Tag from '../Tag/Tag';
// import Tabs from 'react-bootstrap/Tabs';
// import Tab from 'react-bootstrap/Tab';
import { TagFilterType } from '@/app/library/_types/filter-enums';

export default function FicCard({
    data,
    onTagClicked,
}: {
    data: FicCardData;
    onTagClicked: (tagType: TagFilterType, tag: string) => void;
}) {
    const tagTypes = [
        TagFilterType.Warning,
        TagFilterType.Fandom,
        TagFilterType.Ship,
        TagFilterType.Character,
        TagFilterType.Tag,
    ];
    const authors = data.fic.authors ?? ['Anonymous'];
    const rating = data.fic.rating[0];
    const complete = data.fic.complete ? '✅' : '❌';
    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div className='text-lg'>{rating}</div>
                <div className='text-lg'>{complete}</div>
                <div className={`${styles.title} text-base`}>
                    <div>
                        <a href={data.fic.link}>{data.fic.title}</a> by{' '}
                        {authors.map(a => (
                            <Tag key={a} tag={a} onTagClicked={tag => onTagClicked(TagFilterType.Author, tag)}></Tag>
                        ))}
                    </div>
                </div>
            </div>
            <div>
                {tagTypes.map(tagType => {
                    const tags = getTagsByType(data.fic, tagType);
                    return !!tags?.length ? (
                        <TagsBlok key={tagType} tagType={tagType} tags={getTagsByType(data.fic, tagType)}></TagsBlok>
                    ) : null;
                })}
                <span>
                    <strong>Words: </strong>
                    {data.fic.words}
                </span>
            </div>
        </div>
    );
    function TagsBlok({ tagType, tags }: { tagType: TagFilterType; tags: string[] }) {
        const onClick = (t: string) => onTagClicked(tagType, t);
        return (
            <div>
                <span>
                    <strong>{tagType + 's: '}</strong>
                </span>
                {tags?.map(tag => (
                    <Tag key={tag} tag={tag} onTagClicked={onClick} withCross={false}></Tag>
                ))}
            </div>
        );
    }
}
