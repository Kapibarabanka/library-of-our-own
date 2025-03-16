import type { MonthStats } from '$lib/types/api-models';
import type { FicCardData } from '$lib/types/domain-models';

export class HomePageState {
    public startedFics: FicCardData[] = $state([]);
    public stats: MonthStats[] = $state([]);
}

export const pageState = new HomePageState();
