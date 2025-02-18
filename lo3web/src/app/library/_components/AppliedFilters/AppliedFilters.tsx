import Tag from '../Tag/Tag';
import { AppliedFiltersData, TagFilter } from '@/app/library/_types/AppliedFiltersData';
import { FilterInclusion } from '@/app/library/_types/filter-enums';

export default function AppliedFilters({
    appliedFilters,
    onFilterRemoved,
}: {
    appliedFilters: AppliedFiltersData;
    onFilterRemoved: (tagFilter: TagFilter) => void;
}) {
    const showInclude = appliedFilters.HasIncluded;
    const showExclude = appliedFilters.HasExcluded;
    return (
        <>
            {showInclude || showExclude ? (
                <div className='flex flex-col gap-1'>
                    <div>Applied filters (tap filter to remove it):</div>
                    {showInclude ? (
                        <div>
                            <span>Include: </span>
                            {[...appliedFilters.includedTagFilters].map(([tagType, filterValues]) =>
                                [...filterValues].map(tag => (
                                    <Tag
                                        key={tagType + tag}
                                        tag={tag}
                                        onTagClicked={(tag: string) =>
                                            onFilterRemoved({ filterInclusion: FilterInclusion.Include, tagType, tag })
                                        }
                                        withCross={true}></Tag>
                                ))
                            )}
                        </div>
                    ) : null}
                    {showExclude ? (
                        <div>
                            <div>Exclude: </div>
                            {[...appliedFilters.excludedTagFilters].map(([tagType, filterValues]) =>
                                [...filterValues].map(tag => (
                                    <Tag
                                        key={tagType + tag}
                                        tag={tag}
                                        onTagClicked={(tag: string) =>
                                            onFilterRemoved({ filterInclusion: FilterInclusion.Exclude, tagType, tag })
                                        }
                                        withCross={true}></Tag>
                                ))
                            )}
                        </div>
                    ) : null}
                </div>
            ) : null}
        </>
    );
}
