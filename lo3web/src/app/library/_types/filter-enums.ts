export enum FilterInclusion {
    Include = 'Include',
    Exclude = 'Exclude',
}

export enum TagFilterType {
    Ship = 'Ship',
    Tag = 'Tag',
    Fandom = 'Fandom',
    Character = 'Character',
    Author = 'Author',
    Warning = 'Warning',
}

export enum BoolFilterType {
    // Reading = 'Backlog', no need to make it a filter, because I plan to add a separate tab "Reading List"
    OnKindle = 'Is on Kindle',
    Spicy = 'Spicy',
    Read = 'Already read',
}

export enum CustomFilterType {
    Words = 'Word Count',
    Rating = 'Rating',
    Quality = 'Your Rating',
}

export type FilterType = TagFilterType | BoolFilterType | CustomFilterType;
export const filterTypes = [
    ...Object.values(TagFilterType),
    // TODO
    // ...Object.values(BoolFilterType),
    // ...Object.values(CustomFilterType),
];
