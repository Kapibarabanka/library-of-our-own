import z from 'zod';
import { UserFicKeySchema, UserImpressionSchema, type FicCardData } from './domain-models';

export const FinishInfoSchema = z.object({
    key: UserFicKeySchema,
    abandoned: z.boolean(),
    impression: UserImpressionSchema.optional(),
    note: z.string().optional(),
});
export type FinishInfo = z.infer<typeof FinishInfoSchema>;

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

export enum StatTagField {
    Ship = 'Ship',
    Fandom = 'Fandom',
    Tag = 'Tag',
}
export const StatTagFieldSchema = z.enum(StatTagField);

export interface TagDataset {
    tagValue: string;
    counts: number[];
}
export interface TagFieldStats {
    months: string[];
    byFics: TagDataset[];
    byWords: TagDataset[];
}
