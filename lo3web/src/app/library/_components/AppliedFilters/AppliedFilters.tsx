import Tag from '../Tag/Tag';
import { AppliedFiltersData, TagFilter } from '@/app/library/_types/AppliedFiltersData';
import { TagInclusion, TagFiled, BoolField } from '@/app/library/_types/filter-enums';

export default function AppliedFilters({
    appliedFilters,
    onTagFilterRemoved,
    onBoolFilterRemoved,
}: {
    appliedFilters: AppliedFiltersData;
    onTagFilterRemoved: (tagFilter: TagFilter) => void;
    onBoolFilterRemoved: (field: BoolField) => void;
}) {
    const showInclude = appliedFilters.HasIncluded;
    const showExclude = appliedFilters.HasExcluded;
    const showBool = !!appliedFilters.boolFilters.size;
    console.log(showBool);
    return (
        <>
            {appliedFilters.HasFilter ? (
                <div className='flex flex-col gap-1'>
                    <div>Applied filters (tap filter to remove it):</div>
                    {showInclude ? (
                        <AppliedTagFilters
                            filterInclusion={TagInclusion.Include}
                            tagFilters={appliedFilters.includedTagFilters}></AppliedTagFilters>
                    ) : null}
                    {showExclude ? (
                        <AppliedTagFilters
                            filterInclusion={TagInclusion.Exclude}
                            tagFilters={appliedFilters.excludedTagFilters}></AppliedTagFilters>
                    ) : null}
                    {showBool
                        ? [...appliedFilters.boolFilters].map(([field, value]) => (
                              <Tag
                                  key={field}
                                  label={`${field}: ${value ? 'Yes' : 'No'}`}
                                  onTagClicked={() => onBoolFilterRemoved(field)}
                                  withCross={true}></Tag>
                          ))
                        : null}
                </div>
            ) : null}
        </>
    );

    function AppliedTagFilters({
        filterInclusion,
        tagFilters,
    }: {
        filterInclusion: TagInclusion;
        tagFilters: Map<TagFiled, Set<string>>;
    }) {
        return (
            <div>
                <span>{filterInclusion}: </span>
                {[...tagFilters].map(([tagType, filterValues]) =>
                    [...filterValues].map(tag => (
                        <Tag
                            key={tagType + tag}
                            label={tag}
                            onTagClicked={() => onTagFilterRemoved({ filterInclusion: filterInclusion, tagType, tag })}
                            withCross={true}></Tag>
                    ))
                )}
            </div>
        );
    }
}
