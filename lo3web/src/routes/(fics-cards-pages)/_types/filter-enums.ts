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
    Impression = 'Your Impression',
}

export enum FilterType {
    Tag = 'Tag',
    Bool = 'Bool',
    Custom = 'Custom',
}

export type FilterableField = TagField | BoolField | CustomField;
export const filterableFields: FilterableField[] = [
    ...Object.values(TagField),
    ...Object.values(BoolField),
    ...Object.values(CustomField),
];

export type TagFilterItem = {
    value: string;
    count: number;
    label: string;
};
