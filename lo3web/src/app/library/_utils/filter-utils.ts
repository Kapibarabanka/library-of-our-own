import { Fic, FicCardData } from '@/types/domain-models';
import { BoolField, TagField } from '@/app/library/_types/filter-enums';
import { AppliedFiltersData } from '@/app/library/_types/AppliedFiltersData';

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

export function getDisplayedCards(allCards: FicCardData[], appliedFilters: AppliedFiltersData): FicCardData[] {
    let filteredCards = allCards;
    for (const [tagType, filterValues] of appliedFilters.includedTagFilters) {
        const prop = tagFieldToProperty(tagType);
        filteredCards = filteredCards.filter(card => {
            const cardTags: string[] = card.fic[prop] ?? [];
            return [...filterValues].every(filterValue => cardTags.includes(filterValue));
        });
    }
    for (const [tagField, filterValues] of appliedFilters.excludedTagFilters) {
        const prop = tagFieldToProperty(tagField);
        filteredCards = filteredCards.filter(card => {
            const cardTags: string[] = card.fic[prop] ?? [];
            return [...filterValues].every(filterValue => !cardTags.includes(filterValue));
        });
    }
    for (const [boolField, value] of appliedFilters.boolFilters) {
        filteredCards = filteredCards.filter(card => boolFilterApplies(card, boolField, value));
    }
    return filteredCards;
}

function boolFilterApplies(card: FicCardData, boolField: BoolField, value: boolean) {
    switch (boolField) {
        case BoolField.Backlog:
            return card.details.backlog === value;
        case BoolField.OnKindle:
            return card.details.isOnKindle === value;
        case BoolField.Spicy:
            return card.details.spicy === value;
    }
}
