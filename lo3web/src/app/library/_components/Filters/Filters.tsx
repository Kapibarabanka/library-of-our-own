import { useState } from 'react';
import React from 'react';
import AppliedFilters from '../AppliedFilters/AppliedFilters';
import { FiltersState } from '@/app/library/_types/FilterState';
import { AppliedFiltersData, TagFilter } from '@/app/library/_types/AppliedFiltersData';
import { FilterInclusion, filterTypes } from '@/app/library/_types/filter-enums';
import { TagFilterType } from '@/app/library/_types/filter-enums';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import { TagFilterItem } from '../../_types/FiltersData';

export function Filters({
    filtersState,
    onAppliedChanged,
}: {
    filtersState: FiltersState;
    onAppliedChanged: (appliedFilters: AppliedFiltersData) => void;
}) {
    const [filterInclusion, setFilterInclusion] = useState(FilterInclusion.Include);
    const [tagType, setTagType] = useState(TagFilterType.Ship);
    const [autocompleteInput, setAutocompleteInput] = useState('');
    const [autocompleteValue, setAutocompleteValue] = useState<TagFilterItem | null>(null);

    function onTagSelected(tag?: string) {
        if (!!tag) {
            const newApplied = filtersState.appliedFilters.withTagFilter({ filterInclusion, tagType, tag });
            onAppliedChanged(newApplied);
        }
    }
    function onTagFilterRemoved(tagFilter: TagFilter) {
        const newApplied = filtersState.appliedFilters.withoutTagFilter(tagFilter);
        onAppliedChanged(newApplied);
    }
    return (
        <div className='flex flex-col gap-2'>
            <div className='flex flex-col gap-2'>
                <div className='flex flex-row gap-1'>
                    <span>Select filter type:</span>
                    <TextField
                        size='small'
                        select
                        className='flex-1'
                        value={tagType}
                        onChange={e => setTagType(e.target.value)}>
                        {filterTypes.map(filterType => (
                            <MenuItem key={filterType} value={filterType}>
                                {filterType}
                            </MenuItem>
                        ))}
                    </TextField>
                </div>
                <div className='flex flex-row gap-1'>
                    <TextField
                        size='small'
                        select
                        value={filterInclusion}
                        onChange={e => setFilterInclusion(e.target.value)}>
                        {Object.values(FilterInclusion).map(filterType => (
                            <MenuItem key={filterType} value={filterType}>
                                {filterType}
                            </MenuItem>
                        ))}
                    </TextField>
                    <Autocomplete
                        disablePortal
                        className='flex-1'
                        value={autocompleteValue}
                        inputValue={autocompleteInput}
                        clearOnBlur={true}
                        onInputChange={(e, val) => setAutocompleteInput(val)}
                        onClose={() => {
                            setAutocompleteInput('');
                            setAutocompleteValue(null);
                        }}
                        options={filtersState.data.getTagFilterItems(tagType)}
                        onChange={($e, selectedItem) => onTagSelected(selectedItem?.value)}
                        renderInput={params => (
                            <TextField
                                {...params}
                                label={`Select to add ${tagType.toLocaleLowerCase()} filter`}
                                size='small'
                            />
                        )}
                    />
                </div>
            </div>
            <AppliedFilters
                appliedFilters={filtersState.appliedFilters}
                onFilterRemoved={onTagFilterRemoved}></AppliedFilters>
        </div>
    );
}
