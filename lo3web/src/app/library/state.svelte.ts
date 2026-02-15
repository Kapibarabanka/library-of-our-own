import { FicType, Rating, UserImpression, type FicCardData } from '$lib/types/domain-models';
import { SvelteMap, SvelteSet } from 'svelte/reactivity';
import { BoolField, SortBy, SortDirection, TagField, TagInclusion, type TagFilterItem } from './_types/filter-enums';
import { getTagsByField, sortCards, tagFieldToProperty } from './_utils/filter-utils';

const emptyTagFilters: () => [TagField, SvelteSet<string>][] = () =>
    Object.values(TagField).map(tagField => [tagField, new SvelteSet()]);

export class AppliedFiltersState {
    public includedTagFilters: SvelteMap<TagField, SvelteSet<string>> = new SvelteMap<TagField, SvelteSet<string>>(
        emptyTagFilters(),
    );
    public excludedTagFilters: SvelteMap<TagField, SvelteSet<string>> = new SvelteMap<TagField, SvelteSet<string>>(
        emptyTagFilters(),
    );
    public boolFilters: SvelteMap<BoolField, boolean> = new SvelteMap<BoolField, boolean>();
    public allowedRatings: SvelteSet<Rating> = new SvelteSet<Rating>();
    public allowedImpressions: SvelteSet<UserImpression> = new SvelteSet<UserImpression>();

    public sortBy = $state(SortBy.DateAdded);
    public sortDirection = $state(SortDirection.Desc);

    public hasIncluded = $derived(!this.includedTagFilters.values().every(values => ![...values].length));
    public hasExcluded = $derived(!this.excludedTagFilters.values().every(values => ![...values].length));
    public hasApplied = $derived(
        !!this.boolFilters.size ||
            this.hasIncluded ||
            this.hasExcluded ||
            !!this.allowedRatings.size ||
            !!this.allowedImpressions.size,
    );

    public withTagFilter(field: TagField, inclusion: TagInclusion, tag: string) {
        console.log('yoy');
        const map = inclusion === TagInclusion.Include ? this.includedTagFilters : this.excludedTagFilters;
        const set = map.get(field);
        if (set) {
            set.add(tag);
        }
    }

    public withoutTagFilter(field: TagField, inclusion: TagInclusion, tag: string) {
        const map = inclusion === TagInclusion.Include ? this.includedTagFilters : this.excludedTagFilters;
        const set = map.get(field);
        if (set) {
            set.delete(tag);
        }
    }

    public clearFilters() {
        this.boolFilters.clear();
        for (const tagField of Object.values(TagField)) {
            this.includedTagFilters.get(tagField)?.clear();
            this.excludedTagFilters.get(tagField)?.clear();
        }
        this.allowedImpressions.clear();
        this.allowedRatings.clear();
    }
}

export const filterState = new AppliedFiltersState();

export class FicCardsPageState {
    public allCards = $state<FicCardData[]>([]);

    public filteredCards = $derived.by(() => {
        let filteredCards = [...$state.snapshot(this.allCards)];
        for (const [tagType, filterValues] of filterState.includedTagFilters) {
            const prop = tagFieldToProperty(tagType);
            filteredCards = filteredCards.filter(card => {
                const cardTags: string[] = card.ao3Info[prop] ?? [];
                return [...filterValues].every(filterValue => cardTags.includes(filterValue));
            });
        }
        for (const [tagField, filterValues] of filterState.excludedTagFilters) {
            const prop = tagFieldToProperty(tagField);
            filteredCards = filteredCards.filter(card => {
                const cardTags: string[] = card.ao3Info[prop] ?? [];
                return [...filterValues].every(filterValue => !cardTags.includes(filterValue));
            });
        }
        for (const [field, value] of filterState.boolFilters) {
            filteredCards = filteredCards.filter(card => boolFilterApplies(card, field, value));
        }
        if (filterState.allowedRatings.size) {
            filteredCards = filteredCards.filter(card => filterState.allowedRatings.has(card.ao3Info.rating));
        }
        if (filterState.allowedImpressions.size) {
            filteredCards = filteredCards.filter(
                card => card.details.impression && filterState.allowedImpressions.has(card.details.impression),
            );
        }

        return sortCards(filteredCards, filterState.sortBy, filterState.sortDirection);
    });

    public tagFilters = $derived.by(() => {
        const result = new Map<TagField, TagFilterItem[]>();
        for (const tagField of Object.values(TagField)) {
            const includedApplied = filterState.includedTagFilters.get(tagField);
            const excludedApplied = filterState.excludedTagFilters.get(tagField);
            result.set(
                tagField,
                [
                    ...this.filteredCards
                        .flatMap(c => getTagsByField(c.ao3Info, tagField))
                        .reduce(function (storage, item) {
                            storage.set(item, (storage.get(item) ?? 0) + 1);
                            return storage;
                        }, new Map<string, number>()),
                ]
                    .map(([value, count]) => ({
                        value,
                        count,
                        label: `${value} (${count})`,
                        lowercase: value.toLocaleLowerCase(),
                    }))
                    .filter(item => !includedApplied?.has(item.value) && !excludedApplied?.has(item.value))
                    .toSorted((a, b) => b.count - a.count),
            );
        }
        return result;
    });
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
        case BoolField.Series:
            return (card.key.ficType === FicType.Series) === value;
    }
}
