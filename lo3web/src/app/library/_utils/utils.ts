import { Fic, FicCardData } from '@/types/domain-models';
import { TagFilterType } from '@/app/library/_types/filter-enums';
import { AppliedFiltersData } from '@/app/library/_types/AppliedFiltersData';

export type TagFieldName = 'relationships' | 'tags' | 'fandoms' | 'characters' | 'authors' | 'warnings';

export function tagTypeToProperty(tagType: TagFilterType): TagFieldName {
    switch (tagType) {
        case TagFilterType.Ship:
            return 'relationships';
        case TagFilterType.Fandom:
            return 'fandoms';
        case TagFilterType.Character:
            return 'characters';
        case TagFilterType.Author:
            return 'authors';
        case TagFilterType.Warning:
            return 'warnings';
        default:
            return 'tags';
    }
}

export function getTagsByType(fic: Fic, tagType: TagFilterType): string[] {
    const prop = tagTypeToProperty(tagType);
    return fic[prop] ?? [];
}

export function getDisplayedCards(allCards: FicCardData[], appliedFilters: AppliedFiltersData): FicCardData[] {
    let filteredCards = allCards;
    for (const [tagType, filterValues] of appliedFilters.includedTagFilters) {
        const prop = tagTypeToProperty(tagType);
        filteredCards = filteredCards.filter(card => {
            const cardTags: string[] = card.fic[prop] ?? [];
            return [...filterValues].every(filterValue => cardTags.includes(filterValue));
        });
    }
    for (const [tagType, filterValues] of appliedFilters.excludedTagFilters) {
        const prop = tagTypeToProperty(tagType);
        filteredCards = filteredCards.filter(card => {
            const cardTags: string[] = card.fic[prop] ?? [];
            return [...filterValues].every(filterValue => !cardTags.includes(filterValue));
        });
    }
    return filteredCards;
}
