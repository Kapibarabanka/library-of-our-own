'use client';

import { FicCardData } from '@/types/domain-models';
import styles from './card.module.css';
import { getTagsByField } from '@/app/library/_utils/filter-utils';
import Tag from '../Tag/Tag';
import { TagField } from '@/app/library/_types/filter-enums';

export default function FicCard({
    data,
    onTagClicked,
}: {
    data: FicCardData;
    onTagClicked: (tagType: TagField, tag: string) => void;
}) {
    const tagTypes = [TagField.Warning, TagField.Fandom, TagField.Ship, TagField.Character, TagField.Tag];
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
                        {authors.map(author => (
                            <Tag
                                key={author}
                                label={author}
                                onTagClicked={() => onTagClicked(TagField.Author, author)}></Tag>
                        ))}
                    </div>
                </div>
            </div>
            <div>
                {tagTypes.map(tagType => {
                    const tags = getTagsByField(data.fic, tagType);
                    return !!tags?.length ? (
                        <TagsBlok key={tagType} tagType={tagType} tags={getTagsByField(data.fic, tagType)}></TagsBlok>
                    ) : null;
                })}
                <span>
                    <strong>Words: </strong>
                    {data.fic.words}
                </span>
            </div>
        </div>
    );
    function TagsBlok({ tagType, tags }: { tagType: TagField; tags: string[] }) {
        return (
            <div>
                <span>
                    <strong>{tagType + 's: '}</strong>
                </span>
                {tags?.map(tag => (
                    <Tag key={tag} label={tag} onTagClicked={() => onTagClicked(tagType, tag)} withCross={false}></Tag>
                ))}
            </div>
        );
    }
}
