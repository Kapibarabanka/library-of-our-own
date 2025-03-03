export enum FicType {
    Work = 'Work',
    Series = 'Series',
}

export enum Rating {
    None = 'Not Rated',
    General = 'General Audiences',
    Teen = 'Teen And Up Audiences',
    Mature = 'Mature',
    Explicit = 'Explicit',
}

export enum Category {
    FF = 'F/F',
    FM = 'F/M',
    Gen = 'Gen',
    MM = 'M/M',
    Multi = 'Multi',
    Other = 'Other',
    None = 'No categorys',
}

export enum UserImpression {
    Never = 'Never again',
    Meh = 'Meh',
    Ok = 'Ok',
    Nice = 'Nice',
    Brilliant = 'Brilliant',
}

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

export interface FicCardData {
    key: UserFicKey;
    ao3Info: Ao3FicInfo;
    details: FicDetails;
}

export interface FicNote {
    date: string;
    text: string;
}

export interface Fic {
    key: UserFicKey;
    ao3Info: Ao3FicInfo;
    details: FicDetails;
    notes: FicNote[];
}
