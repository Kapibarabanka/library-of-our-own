import { UserImpression, type Fic, type FicCardData } from '$lib/types/domain-models';
import { BoolField, FilterType, SortBy, SortDirection, TagField, type FilterableField } from '../_types/filter-enums';

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

export function sortCards(cards: FicCardData[], sortBy: SortBy, direction: SortDirection): FicCardData[] {
    switch (sortBy) {
        case SortBy.DateAdded:
            return cards.sort((a, b) => {
                let result = 0;
                if (a.details.recordCreated > b.details.recordCreated) {
                    result = 1;
                } else if (a.details.recordCreated < b.details.recordCreated) {
                    result = -1;
                }
                if (direction === SortDirection.Desc) {
                    result = result * -1;
                }
                return result;
            });
        case SortBy.WordCount:
            return cards.sort((a, b) =>
                direction === SortDirection.Asc ? a.fic.words - b.fic.words : b.fic.words - a.fic.words
            );
        case SortBy.Impression:
            return cards.sort((a, b) => {
                const aImpr = impressionToNumber(a.details.impression);
                const bImpr = impressionToNumber(b.details.impression);
                return direction === SortDirection.Asc ? aImpr - bImpr : bImpr - aImpr;
            });
    }
    return cards;
}

function impressionToNumber(impression?: UserImpression | undefined) {
    if (!impression) {
        return 0;
    }
    switch (impression) {
        case UserImpression.Never:
            return 1;
        case UserImpression.Meh:
            return 2;
        case UserImpression.Ok:
            return 3;
        case UserImpression.Nice:
            return 4;
        case UserImpression.Brilliant:
            return 5;
    }
}
