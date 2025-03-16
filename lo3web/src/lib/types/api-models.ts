import type { FicCardData, UserFicKey, UserImpression } from './domain-models';

export interface FinishInfo {
    key: UserFicKey;
    abandoned: boolean;
    impression?: UserImpression | null | undefined;
    note?: string | null | undefined;
}

export interface MonthStats {
    month: string;
    fics: number;
    words: number;
}

export interface HomePageData {
    currentlyReading: FicCardData[];
    randomFicFromBacklog?: FicCardData | null;
    generalStats: MonthStats[];
}
