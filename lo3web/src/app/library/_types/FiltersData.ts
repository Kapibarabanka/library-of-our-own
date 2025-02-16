import { FicCardData } from '@/types/domain-models';
import { AppliedFiltersData } from './AppliedFiltersData';
import { TagFilterType } from './filter-enums';
import { getTagsByType } from '../_utils/utils';

export type TagFilterItem = {
    value: string;
    count: number;
    label: string;
};

export class FiltersData {
    public tagFilters: Map<TagFilterType, TagFilterItem[]>;
    constructor(cards: FicCardData[], appliedFilters: AppliedFiltersData) {
        const tagFilters: Iterable<[TagFilterType, TagFilterItem[]]> = Object.values(TagFilterType).map(tagType => [
            tagType,
            this.toFilterItems(
                cards.flatMap(c => getTagsByType(c.fic, tagType)),
                appliedFilters,
                tagType
            ),
        ]);
        this.tagFilters = new Map(tagFilters);
    }
    public getTagFilterItems(tagType: TagFilterType): TagFilterItem[] {
        return this.tagFilters.get(tagType) ?? [];
    }
    private toFilterItems(
        values: string[],
        appliedFilters: AppliedFiltersData,
        tagType: TagFilterType
    ): TagFilterItem[] {
        const includedApplied = appliedFilters.includedTagFilters.get(tagType);
        const excludedApplied = appliedFilters.excludedTagFilters.get(tagType);
        return [
            ...values.reduce(function (storage, item) {
                storage.set(item, (storage.get(item) ?? 0) + 1);
                return storage;
            }, new Map<string, number>()),
        ]
            .map(([value, count]) => ({ value, count, label: `${value} (${count})` }))
            .filter(item => !includedApplied?.has(item.value) && !excludedApplied?.has(item.value))
            .toSorted((a, b) => b.count - a.count);
    }
}
