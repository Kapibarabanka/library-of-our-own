import z from 'zod';

export enum FicType {
    Work = 'Work',
    Series = 'Series',
}
export const FicTypeSchema = z.enum(FicType);

export enum Rating {
    None = 'Not Rated',
    General = 'General Audiences',
    Teen = 'Teen And Up Audiences',
    Mature = 'Mature',
    Explicit = 'Explicit',
}
export const RatingSchema = z.enum(Rating);

export enum Category {
    FF = 'F/F',
    FM = 'F/M',
    Gen = 'Gen',
    MM = 'M/M',
    Multi = 'Multi',
    Other = 'Other',
    None = 'No categorys',
}
export const CategorySchema = z.enum(Category);

export enum UserImpression {
    Never = 'Never again',
    Meh = 'Meh',
    Ok = 'Ok',
    Nice = 'Nice',
    Brilliant = 'Brilliant',
}
export const UserImpressionSchema = z.enum(UserImpression);

export interface Ao3FicInfo {
    id: string;
    link: string;
    ficType: FicType;
    title: string;
    authors?: string[];
    rating: Rating;
    categories?: Category[];
    warnings?: string[];
    fandoms?: string[];
    characters?: string[];
    relationships?: string[];
    tags?: string[];
    words: number;
    complete: boolean;
}

export interface FicDetails {
    backlog: boolean;
    isOnKindle: boolean;
    impression?: UserImpression;
    spicy: boolean;
    recordCreated: string;
}

export interface UserFicKey {
    userId: string;
    ficId: string;
    ficType: FicType;
}
export const UserFicKeySchema = z.object({
    userId: z.string(),
    ficId: z.string(),
    ficType: FicTypeSchema,
});

export const FicKeySchema = z.object({
    ficId: z.string(),
    ficType: FicTypeSchema,
});
export type FicKey = z.infer<typeof FicKeySchema>;

export interface FicCardData {
    key: UserFicKey;
    ao3Info: Ao3FicInfo;
    details: FicDetails;
}

export interface FicNote {
    date: string;
    text: string;
}

export interface ReadDates {
    startDate: string;
    finishDate?: string | undefined;
    isAbandoned: boolean;
}

export interface ReadDatesInfo {
    readDates: ReadDates[];
    canStart: boolean;
    canFinish: boolean;
}

export interface Fic {
    userId: string;
    ao3Info: Ao3FicInfo;
    details: FicDetails;
    readDatesInfo: ReadDatesInfo;
    notes: FicNote[];
}
