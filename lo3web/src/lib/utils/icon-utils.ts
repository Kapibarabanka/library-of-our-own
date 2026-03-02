import { Rating, UserImpression } from '$lib/types/domain-models';
import { BoolField, CustomField, TagField, type FilterableField } from '@app/library/_types/filter-enums';
import {
    Heart,
    Tablet,
    ListTodo,
    Hash,
    Earth,
    User,
    BookUser,
    TriangleAlert,
    Flame,
    BookCopy,
    Calculator,
    IdCard,
    MessageCircleHeart,
    type Icon as IconType,
} from '@lucide/svelte';

export function getRatingBackground(rating: Rating) {
    switch (rating) {
        case Rating.General:
            return 'bg-lime-300';
        case Rating.Teen:
            return 'bg-yellow-200';
        case Rating.Mature:
            return 'bg-orange-300';
        case Rating.Explicit:
            return 'bg-red-300';
        default:
            return 'bg-inherit';
    }
}

export function getImpressionIcon(impression: UserImpression) {
    switch (impression) {
        case UserImpression.Never:
            return '⛔️';
        case UserImpression.Meh:
            return '🫤';
        case UserImpression.Ok:
            return '🍺';
        case UserImpression.Nice:
            return '💜';
        case UserImpression.Brilliant:
            return '🦄';
    }
}
export const filterIcons: Record<FilterableField, typeof IconType> = {
    [TagField.relationships]: Heart,
    [TagField.freeformTags]: Hash,
    [TagField.fandoms]: Earth,
    [TagField.characters]: User,
    [TagField.authors]: BookUser,
    [TagField.warnings]: TriangleAlert,
    [BoolField.Backlog]: ListTodo,
    [BoolField.OnKindle]: Tablet,
    [BoolField.Spicy]: Flame,
    [BoolField.Series]: BookCopy,
    [CustomField.Words]: Calculator,
    [CustomField.Rating]: IdCard,
    [CustomField.Impression]: MessageCircleHeart,
};
