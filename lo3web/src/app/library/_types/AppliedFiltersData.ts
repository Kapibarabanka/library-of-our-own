import { Rating } from '@/types/domain-models';
import { FilterInclusion, TagFilterType } from './filter-enums';

export type TagFilter = {
    filterInclusion: FilterInclusion;
    tagType: TagFilterType;
    tag: string;
};

export class AppliedFiltersData {
    public includedTagFilters: Map<TagFilterType, Set<string>>;
    public excludedTagFilters: Map<TagFilterType, Set<string>>;
    public allowedRatings: Set<Rating>;

    constructor(filters: Partial<AppliedFiltersData>) {
        this.includedTagFilters =
            filters.includedTagFilters ??
            new Map<TagFilterType, Set<string>>(Object.values(TagFilterType).map(tagType => [tagType, new Set()]));
        this.excludedTagFilters =
            filters.excludedTagFilters ??
            new Map<TagFilterType, Set<string>>(Object.values(TagFilterType).map(tagType => [tagType, new Set()]));
        this.allowedRatings = filters.allowedRatings ?? new Set<Rating>();
    }

    public get HasIncluded() {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        return ![...this.includedTagFilters].every(([_, values]) => ![...values].length);
    }

    public get HasExcluded() {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        return ![...this.excludedTagFilters].every(([_, values]) => ![...values].length);
    }

    public get HasAllowedRatings() {
        return !!this.allowedRatings.size;
    }

    public withTagFilter(filter: TagFilter) {
        return this.tagFilterAction(filter, true);
    }
    public withoutTagFilter(filter: TagFilter) {
        return this.tagFilterAction(filter, false);
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
            filter.filterInclusion === FilterInclusion.Include ? copy.includedTagFilters : copy.excludedTagFilters;
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
