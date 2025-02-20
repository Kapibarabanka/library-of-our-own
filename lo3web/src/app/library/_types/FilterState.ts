import { FicCardData } from '@/types/domain-models';
import { AppliedFiltersData } from './AppliedFiltersData';
import { FiltersData } from './FiltersData';

export class FiltersState {
    public data: FiltersData;
    constructor(
        public displayedCards: FicCardData[],
        public appliedFilters: AppliedFiltersData = new AppliedFiltersData({})
    ) {
        this.data = new FiltersData(displayedCards, appliedFilters);
    }
}
