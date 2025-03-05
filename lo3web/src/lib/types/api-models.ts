import type { FicCardData } from './domain-models';

export interface HomePageData {
    currentlyReading: FicCardData[];
    randomFicFromBacklog?: FicCardData | null;
}
