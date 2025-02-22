import type { FicCardData } from '$lib/types/domain-models';

interface AppliedFilters {
    isSpicy?: boolean | undefined;
}

export class FicCardsPageState {
    public allCards = $state<FicCardData[]>([]);
    public appliedFilters = $state<AppliedFilters>({});
    public filteredCards = $derived.by(() => {
        if (this.appliedFilters.isSpicy === undefined) return this.allCards;
        return this.allCards.filter(card => card.details.spicy === this.appliedFilters.isSpicy);
    });
}

export const pageState = new FicCardsPageState();
