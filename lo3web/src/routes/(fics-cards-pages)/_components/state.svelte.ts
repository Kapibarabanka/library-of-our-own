import type { FicCardData } from '$lib/types/domain-models';
import { SvelteMap, SvelteSet } from 'svelte/reactivity';
import { BoolField, TagField, TagInclusion, type TagFilterItem } from '../_types/filter-enums';
import { getTagsByField } from '../_utils/filter-utils';

interface AppliedFilters {
    includedTagFilters: SvelteMap<TagField, SvelteSet<string>>;
    excludedTagFilters: SvelteMap<TagField, SvelteSet<string>>;
    boolFilters: SvelteMap<BoolField, boolean>;
}

const emptyTagFilters: () => [TagField, SvelteSet<string>][] = () =>
    Object.values(TagField).map(tagField => [tagField, new SvelteSet()]);

export class FicCardsPageState {
    public allCards = $state<FicCardData[]>([]);
    public appliedFilters = $state<AppliedFilters>({
        boolFilters: new SvelteMap<BoolField, boolean>(),
        includedTagFilters: new SvelteMap<TagField, SvelteSet<string>>(emptyTagFilters()),
        excludedTagFilters: new SvelteMap<TagField, SvelteSet<string>>(emptyTagFilters()),
    });
    public hasIncluded = $derived(
        !this.appliedFilters.includedTagFilters.values().every(values => ![...values].length)
    );
    public hasExcluded = $derived(
        !this.appliedFilters.excludedTagFilters.values().every(values => ![...values].length)
    );
    public hasApplied = $derived(!!this.appliedFilters.boolFilters.size || this.hasIncluded || this.hasExcluded);

    public filteredCards = $derived.by(() => {
        let filteredCards = [...this.allCards];
        for (const [tagType, filterValues] of this.appliedFilters.includedTagFilters) {
            const prop = tagFieldToProperty(tagType);
            filteredCards = filteredCards.filter(card => {
                const cardTags: string[] = card.fic[prop] ?? [];
                return [...filterValues].every(filterValue => cardTags.includes(filterValue));
            });
        }
        for (const [tagField, filterValues] of this.appliedFilters.excludedTagFilters) {
            const prop = tagFieldToProperty(tagField);
            filteredCards = filteredCards.filter(card => {
                const cardTags: string[] = card.fic[prop] ?? [];
                return [...filterValues].every(filterValue => !cardTags.includes(filterValue));
            });
        }
        for (const [field, value] of this.appliedFilters.boolFilters) {
            filteredCards = filteredCards.filter(card => boolFilterApplies(card, field, value));
        }
        return filteredCards;
    });

    public tagFilters = $derived.by(() => {
        const result = new Map<TagField, TagFilterItem[]>();
        for (const tagField of Object.values(TagField)) {
            const includedApplied = this.appliedFilters.includedTagFilters.get(tagField);
            const excludedApplied = this.appliedFilters.excludedTagFilters.get(tagField);
            result.set(
                tagField,
                [
                    ...this.filteredCards
                        .flatMap(c => getTagsByField(c.fic, tagField))
                        .reduce(function (storage, item) {
                            storage.set(item, (storage.get(item) ?? 0) + 1);
                            return storage;
                        }, new Map<string, number>()),
                ]
                    .map(([value, count]) => ({ value, count, label: `${value} (${count})` }))
                    .filter(item => !includedApplied?.has(item.value) && !excludedApplied?.has(item.value))
                    .toSorted((a, b) => b.count - a.count)
            );
        }
        return result;
    });

    public withTagFilter(field: TagField, inclusion: TagInclusion, tag: string) {
        const map =
            inclusion === TagInclusion.Include
                ? this.appliedFilters.includedTagFilters
                : this.appliedFilters.excludedTagFilters;
        const set = map.get(field);
        if (set) {
            set.add(tag);
        }
    }

    public withoutTagFilter(field: TagField, inclusion: TagInclusion, tag: string) {
        const map =
            inclusion === TagInclusion.Include
                ? this.appliedFilters.includedTagFilters
                : this.appliedFilters.excludedTagFilters;
        const set = map.get(field);
        if (set) {
            set.delete(tag);
        }
    }
}

export const pageState = new FicCardsPageState();

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
