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
    Backlog = 'Backlog',
    OnKindle = 'Is on Kindle',
    Spicy = 'Spicy',
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

export function getFilterType(filteredField: FilterableField): FilterType {
    if (Object.values(TagField).includes(filteredField as TagField)) return FilterType.Tag;
    if (Object.values(BoolField).includes(filteredField as BoolField)) return FilterType.Bool;
    return FilterType.Custom;
}

export type FilterableField = TagField | BoolField | CustomField;
export const filterableFields = [
    ...Object.values(TagField),
    // TODO
    ...Object.values(BoolField),
    // ...Object.values(CustomFilterType),
];
