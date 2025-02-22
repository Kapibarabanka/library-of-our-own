import type { FicCardData } from './domain-models';

export interface FicsPageRequest {
    userId: string;
    pageSize: number;
    pageNumber: number;
}

export interface FicsPage {
    total: number;
    cards: FicCardData[];
}
