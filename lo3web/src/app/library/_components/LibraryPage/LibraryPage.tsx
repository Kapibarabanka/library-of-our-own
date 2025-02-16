'use client';

import { FicCardData } from '@/types/domain-models';
import { useState } from 'react';
import { FiltersData } from '../../_types/FiltersData';
import { AppliedFiltersData } from '../../_types/AppliedFiltersData';
import { FiltersState } from '../../_types/FilterState';
import { getDisplayedCards } from '../../_utils/utils';
import { FilterInclusion, TagFilterType } from '../../_types/filter-enums';
import { Filters } from '../Filters/Filters';
// import Form from 'react-bootstrap/Form';
import FicCard from '../FicCard/FicCard';

export default function LibraryPage({ allCards }: { allCards: FicCardData[] }) {
    const initialFiltersData = new FiltersData(allCards, new AppliedFiltersData({}));
    const [displayedCards, setDisplayedCards] = useState(allCards);
    const [filtersState, setFiltersState] = useState(new FiltersState(initialFiltersData));
    function handleFilterChange(appliedFilters: AppliedFiltersData) {
        const newDisplayedCards = getDisplayedCards(allCards, appliedFilters);
        setFiltersState(new FiltersState(new FiltersData(newDisplayedCards, appliedFilters), appliedFilters));
        setDisplayedCards(newDisplayedCards);
    }
    function onTagClicked(tagType: TagFilterType, tag: string) {
        const newApplied = filtersState.appliedFilters.withTagFilter({
            filterInclusion: FilterInclusion.Include,
            tagType,
            tag,
        });
        handleFilterChange(newApplied);
    }
    const showCount = filtersState.appliedFilters.HasIncluded || filtersState.appliedFilters.HasExcluded;
    return (
        <div className='flex flex-col gap-2 p-2'>
            <Filters filtersState={filtersState} onAppliedChanged={handleFilterChange}></Filters>
            <div className='flex flex-col gap-1'>
                {showCount ? (
                    <span>Filtered results ({displayedCards.length}):</span>
                ) : // <Form.Text className='text-muted'>Filtered results ({displayedCards.length}):</Form.Text>
                null}
                <div className='flex flex-col gap-2'>
                    {displayedCards.map(cardData => (
                        <FicCard
                            key={cardData.fic.ficType + cardData.fic.id}
                            data={cardData}
                            onTagClicked={onTagClicked}></FicCard>
                    ))}
                </div>
            </div>
        </div>
    );
}
