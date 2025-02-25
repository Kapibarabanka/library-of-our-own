import { Rating, UserImpression } from '$lib/types/domain-models';

export function getRatingBackground(rating: Rating) {
    switch (rating) {
        case Rating.General:
            return 'bg-lime-300';
        case Rating.Teen:
            return 'bg-yellow-300';
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
