import type { Fic } from '$lib/types/domain-models';
import { BoolField, FilterType, TagField, type FilterableField } from '../_types/filter-enums';

export type TagFieldName = 'relationships' | 'tags' | 'fandoms' | 'characters' | 'authors' | 'warnings';

export function tagFieldToProperty(tagType: TagField): TagFieldName {
    switch (tagType) {
        case TagField.Ship:
            return 'relationships';
        case TagField.Fandom:
            return 'fandoms';
        case TagField.Character:
            return 'characters';
        case TagField.Author:
            return 'authors';
        case TagField.Warning:
            return 'warnings';
        default:
            return 'tags';
    }
}

export function getTagsByField(fic: Fic, tagField: TagField): string[] {
    const prop = tagFieldToProperty(tagField);
    return fic[prop] ?? [];
}

export function getFilterType(filteredField: FilterableField): FilterType {
    if (Object.values(TagField).includes(filteredField as TagField)) return FilterType.Tag;
    if (Object.values(BoolField).includes(filteredField as BoolField)) return FilterType.Bool;
    return FilterType.Custom;
}
