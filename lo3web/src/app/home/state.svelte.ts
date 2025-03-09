import type { FicCardData } from '$lib/types/domain-models';

export class HomePageState {
    public startedFics: FicCardData[] = $state([]);
}

export const pageState = new HomePageState();
