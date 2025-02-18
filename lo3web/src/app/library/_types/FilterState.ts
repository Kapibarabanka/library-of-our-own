import { AppliedFiltersData } from './AppliedFiltersData';
import { FiltersData } from './FiltersData';

export class FiltersState {
    constructor(public data: FiltersData, public appliedFilters: AppliedFiltersData = new AppliedFiltersData({})) {}
}
