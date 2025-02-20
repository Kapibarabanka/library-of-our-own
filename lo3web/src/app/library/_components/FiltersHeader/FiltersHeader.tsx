import { useState } from 'react';
import React from 'react';
import AppliedFilters from '../AppliedFilters/AppliedFilters';
import { FiltersState } from '@/app/library/_types/FilterState';
import { AppliedFiltersData, TagFilter } from '@/app/library/_types/AppliedFiltersData';
import {
    BoolField,
    FilterableField,
    filterableFields,
    FilterType,
    getFilterType,
    TagInclusion,
} from '@/app/library/_types/filter-enums';
import { TagFiled } from '@/app/library/_types/filter-enums';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import TagFilterComponent from '../FilterComponents/TagFilterComponent';
import BoolFilterComponent from '../FilterComponents/BoolFilterComponent';

export function Filters({
    filtersState,
    onAppliedChanged,
}: {
    filtersState: FiltersState;
    onAppliedChanged: (appliedFilters: AppliedFiltersData) => void;
}) {
    const [filteredFiled, setFilteredField] = useState<FilterableField>(TagFiled.Ship);
    const [filterType, setFilterType] = useState(FilterType.Tag);

    function onFilterTypeSelected(selectedType: FilterableField) {
        setFilteredField(selectedType);
        setFilterType(getFilterType(selectedType));
    }

    function onTagSelected(tagInclusion: TagInclusion, tag?: string) {
        if (!!tag) {
            const tagField = filteredFiled as TagFiled;
            const newApplied = filtersState.appliedFilters.withTagFilter({
                filterInclusion: tagInclusion,
                tagType: tagField,
                tag,
            });
            onAppliedChanged(newApplied);
        }
    }

    function onBoolOptionSelected(selection: boolean) {
        const newApplied = filtersState.appliedFilters.withBoolFilter(filteredFiled as BoolField, selection);
        onAppliedChanged(newApplied);
    }

    function onTagFilterRemoved(tagFilter: TagFilter) {
        const newApplied = filtersState.appliedFilters.withoutTagFilter(tagFilter);
        onAppliedChanged(newApplied);
    }

    function onBoolFilterRemoved(field: BoolField) {
        const newApplied = filtersState.appliedFilters.withoutBoolFilter(field);
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
                        value={filteredFiled}
                        onChange={e => onFilterTypeSelected(e.target.value as FilterableField)}>
                        {filterableFields.map(filterType => (
                            <MenuItem key={filterType} value={filterType}>
                                {filterType}
                            </MenuItem>
                        ))}
                    </TextField>
                </div>
                <Filter />
            </div>
            <AppliedFilters
                appliedFilters={filtersState.appliedFilters}
                onTagFilterRemoved={onTagFilterRemoved}
                onBoolFilterRemoved={onBoolFilterRemoved}></AppliedFilters>
        </div>
    );

    function Filter() {
        switch (filterType) {
            case FilterType.Tag:
                return (
                    <TagFilterComponent
                        filteredField={filteredFiled}
                        filterItems={filtersState.data.getTagFilterItems(filteredFiled as TagFiled)}
                        onTagSelected={onTagSelected}
                    />
                );
            case FilterType.Bool:
                return (
                    <BoolFilterComponent
                        defaultValue={filtersState.appliedFilters.boolFilters.get(filteredFiled as BoolField)}
                        onOptionSelected={onBoolOptionSelected}
                    />
                );
            default:
                return <div>Cuatom filter</div>;
        }
    }
}
