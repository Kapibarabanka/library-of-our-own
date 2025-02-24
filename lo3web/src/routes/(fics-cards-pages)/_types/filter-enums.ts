export enum TagInclusion {
    Include = 'Include',
    Exclude = 'Exclude',
}

export enum TagField {
    Ship = 'Ship',
    Tag = 'Tag',
    Fandom = 'Fandom',
    Character = 'Character',
    Author = 'Author',
    Warning = 'Warning',
}

export enum BoolField {
    Backlog = 'In Reading List',
    OnKindle = 'On Kindle',
    Spicy = 'Spicy',
    Series = 'Series',
}

export enum CustomField {
    Words = 'Word Count',
    Rating = 'Rating',
    Quality = 'Your Rating',
}

export enum FilterType {
    Tag = 'Tag',
    Bool = 'Bool',
    Custom = 'Custom',
}

export type FilterableField = TagField | BoolField | CustomField;
export const filterableFields: FilterableField[] = [
    ...Object.values(TagField),
    // TODO
    ...Object.values(BoolField),
    // ...Object.values(CustomFilterType),
];

export type TagFilterItem = {
    value: string;
    count: number;
    label: string;
};
