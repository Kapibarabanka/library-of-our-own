import type { FicCardData } from "$lib/types/domain-models";

export interface PageState {
    allCards: FicCardData[];
}
export const page = $state<PageState>({allCards: []})