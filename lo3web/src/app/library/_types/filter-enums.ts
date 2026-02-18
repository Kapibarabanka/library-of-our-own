export enum TagInclusion {
    Include = 'Include',
    Exclude = 'Exclude',
}

export enum TagField {
    relationships = 'relationships',
    tags = 'tags',
    fandoms = 'fandoms',
    characters = 'characters',
    authors = 'authors',
    warnings = 'warnings',
}
export const tagFieldLabels: Record<TagField, string> = {
    [TagField.relationships]: 'Relationship',
    [TagField.tags]: 'Freeform Tag',
    [TagField.fandoms]: 'Fandom',
    [TagField.characters]: 'Character',
    [TagField.authors]: 'Author',
    [TagField.warnings]: 'Warning',
};

export enum BoolField {
    Backlog = 'In Backlog',
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
export const ao3Fields = [
    ...Object.values(TagField).map(field => ({ field, label: tagFieldLabels[field] })),
    ...[CustomField.Rating, CustomField.Words, BoolField.Series].map(field => ({ field, label: field })),
];
export const userFields = [CustomField.Impression, BoolField.Spicy, BoolField.Backlog, BoolField.OnKindle].map(
    field => ({ field, label: field }),
);
export const filterableFields: { field: FilterableField; label: string }[] = [...ao3Fields, ...userFields];

export type TagFilterItem = {
    value: string;
    count: number;
    label: string;
    lowercase: string;
};

export enum SortBy {
    DateAdded = 'Date Added',
    WordCount = 'Word Count',
    Impression = 'Your Impression',
}

export enum SortDirection {
    Asc = 'Asc',
    Desc = 'Desc',
}
