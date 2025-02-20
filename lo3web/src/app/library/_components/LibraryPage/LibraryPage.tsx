'use client';

import { FicCardData } from '@/types/domain-models';
import { useState } from 'react';
import { AppliedFiltersData } from '../../_types/AppliedFiltersData';
import { FiltersState } from '../../_types/FilterState';
import { getDisplayedCards } from '../../_utils/filter-utils';
import { TagInclusion, TagFiled } from '../../_types/filter-enums';
import { Filters } from '../FiltersHeader/FiltersHeader';
import FicCard from '../FicCard/FicCard';

export default function LibraryPage({ allCards }: { allCards: FicCardData[] }) {
    const [displayedCards, setDisplayedCards] = useState(allCards);
    const [filtersState, setFiltersState] = useState(new FiltersState(allCards));
    function handleFilterChange(appliedFilters: AppliedFiltersData) {
        const newDisplayedCards = getDisplayedCards(allCards, appliedFilters);
        setFiltersState(new FiltersState(newDisplayedCards, appliedFilters));
        setDisplayedCards(newDisplayedCards);
    }
    function onTagClicked(tagType: TagFiled, tag: string) {
        const newApplied = filtersState.appliedFilters.withTagFilter({
            filterInclusion: TagInclusion.Include,
            tagType,
            tag,
        });
        handleFilterChange(newApplied);
    }
    return (
        <div className='flex flex-col gap-2 p-2'>
            <Filters filtersState={filtersState} onAppliedChanged={handleFilterChange}></Filters>
            <div className='flex flex-col gap-1'>
                {filtersState.appliedFilters.HasFilter ? (
                    <span>Filtered results ({displayedCards.length}):</span>
                ) : null}
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
