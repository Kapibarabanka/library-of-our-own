import { FicCardData } from '@/types/domain-models';
import { AppliedFiltersData } from './AppliedFiltersData';
import { TagFiled } from './filter-enums';
import { getTagsByField } from '../_utils/filter-utils';

export type TagFilterItem = {
    value: string;
    count: number;
    label: string;
};

export class FiltersData {
    public tagFilters: Map<TagFiled, TagFilterItem[]>;
    constructor(cards: FicCardData[], appliedFilters: AppliedFiltersData) {
        const tagFilters: Iterable<[TagFiled, TagFilterItem[]]> = Object.values(TagFiled).map(tagType => [
            tagType,
            this.toFilterItems(
                cards.flatMap(c => getTagsByField(c.fic, tagType)),
                appliedFilters,
                tagType
            ),
        ]);
        this.tagFilters = new Map(tagFilters);
    }
    public getTagFilterItems(tagType: TagFiled): TagFilterItem[] {
        return this.tagFilters.get(tagType) ?? [];
    }
    private toFilterItems(values: string[], appliedFilters: AppliedFiltersData, tagType: TagFiled): TagFilterItem[] {
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
