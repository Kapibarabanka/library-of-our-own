import type { FicCardData, UserFicKey, UserImpression } from './domain-models';

export interface HomePageData {
    currentlyReading: FicCardData[];
    randomFicFromBacklog?: FicCardData | null;
}

export interface FinishInfo {
    key: UserFicKey;
    abandoned: boolean;
    impression?: UserImpression | null | undefined;
    note?: string | null | undefined;
}
