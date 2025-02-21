import { Rating } from '@/types/domain-models';
import { TagInclusion, TagField, BoolField } from './filter-enums';

export type TagFilter = {
    filterInclusion: TagInclusion;
    tagType: TagField;
    tag: string;
};

export class AppliedFiltersData {
    public includedTagFilters: Map<TagField, Set<string>>;
    public excludedTagFilters: Map<TagField, Set<string>>;
    public allowedRatings: Set<Rating>;
    public boolFilters: Map<BoolField, boolean>;

    constructor(filters: Partial<AppliedFiltersData>) {
        this.includedTagFilters =
            filters.includedTagFilters ??
            new Map<TagField, Set<string>>(Object.values(TagField).map(tagType => [tagType, new Set()]));
        this.excludedTagFilters =
            filters.excludedTagFilters ??
            new Map<TagField, Set<string>>(Object.values(TagField).map(tagType => [tagType, new Set()]));
        this.allowedRatings = filters.allowedRatings ?? new Set<Rating>();
        this.boolFilters = filters.boolFilters ?? new Map<BoolField, boolean>();
    }

    public get HasFilter() {
        return this.HasIncluded || this.HasExcluded || this.allowedRatings.size || this.boolFilters.size;
    }

    public get HasIncluded() {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        return ![...this.includedTagFilters].every(([_, values]) => ![...values].length);
    }

    public get HasExcluded() {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        return ![...this.excludedTagFilters].every(([_, values]) => ![...values].length);
    }

    public withTagFilter(filter: TagFilter) {
        return this.tagFilterAction(filter, true);
    }
    public withoutTagFilter(filter: TagFilter) {
        return this.tagFilterAction(filter, false);
    }

    public withBoolFilter(field: BoolField, value: boolean) {
        const copy = new AppliedFiltersData(this);
        copy.boolFilters.set(field, value);
        return copy;
    }

    public withoutBoolFilter(field: BoolField) {
        const copy = new AppliedFiltersData(this);
        copy.boolFilters.delete(field);
        return copy;
    }

    public withRating(rating: Rating) {
        const copy = new AppliedFiltersData(this);
        this.allowedRatings.add(rating);
        return copy;
    }

    public withoutRating(rating: Rating) {
        const copy = new AppliedFiltersData(this);
        this.allowedRatings.delete(rating);
        return copy;
    }

    private tagFilterAction(filter: TagFilter, isAdd: boolean) {
        const copy = new AppliedFiltersData(this);
        const filterMap =
            filter.filterInclusion === TagInclusion.Include ? copy.includedTagFilters : copy.excludedTagFilters;
        const filterSet = filterMap.get(filter.tagType) ?? new Set<string>();
        if (isAdd) {
            filterSet.add(filter.tag);
        } else {
            filterSet.delete(filter.tag);
        }
        filterMap.set(filter.tagType, filterSet);
        return copy;
    }
}
