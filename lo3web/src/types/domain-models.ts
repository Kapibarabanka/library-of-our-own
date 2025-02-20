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

export enum Quality {
    Never = 'Never again',
    Meh = 'Meh',
    Ok = 'Ok',
    Nice = 'Nice',
    Brilliant = 'Brilliant',
}

export type Fic = {
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
};

export type FicDetails = {
    backlog: boolean;
    isOnKindle: boolean;
    quality?: Quality;
    spicy: boolean;
};

export type UserFicKey = {
    userId: string;
    ficId: string;
    ficType: FicType;
};

export type FicCardData = {
    key: UserFicKey;
    fic: Fic;
    details: FicDetails;
};
