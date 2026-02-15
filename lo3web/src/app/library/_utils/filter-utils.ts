import { UserImpression, type FicCardData } from '$lib/types/domain-models';
import { BoolField, FilterType, SortBy, SortDirection, TagField, type FilterableField } from '../_types/filter-enums';

export function getFilterType(filteredField: FilterableField): FilterType {
    if (Object.values(TagField).includes(filteredField as TagField)) return FilterType.Tag;
    if (Object.values(BoolField).includes(filteredField as BoolField)) return FilterType.Bool;
    return FilterType.Custom;
}

export function sortCards(cards: FicCardData[], sortBy: SortBy, direction: SortDirection): FicCardData[] {
    switch (sortBy) {
        case SortBy.DateAdded:
            return cards.sort((a, b) => {
                let result = 0;
                if ((a.details.recordCreated ?? 0) > (b.details.recordCreated ?? 0)) {
                    result = 1;
                } else if ((a.details.recordCreated ?? 0) < (b.details.recordCreated ?? 0)) {
                    result = -1;
                }
                if (direction === SortDirection.Desc) {
                    result = result * -1;
                }
                return result;
            });
        case SortBy.WordCount:
            return cards.sort((a, b) =>
                direction === SortDirection.Asc ? a.ao3Info.words - b.ao3Info.words : b.ao3Info.words - a.ao3Info.words,
            );
        case SortBy.Impression:
            return cards.sort((a, b) => {
                const aImpr = impressionToNumber(a.details.impression);
                const bImpr = impressionToNumber(b.details.impression);
                return direction === SortDirection.Asc ? aImpr - bImpr : bImpr - aImpr;
            });
    }
}

function impressionToNumber(impression?: UserImpression | undefined) {
    if (!impression) {
        return 0;
    }
    switch (impression) {
        case UserImpression.Never:
            return 1;
        case UserImpression.Meh:
            return 2;
        case UserImpression.Ok:
            return 3;
        case UserImpression.Nice:
            return 4;
        case UserImpression.Brilliant:
            return 5;
    }
}
