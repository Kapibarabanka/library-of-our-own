import { Rating } from '$lib/types/domain-models';

export function shortRating(rating: Rating) {
    switch (rating) {
        case Rating.None:
            return 'None';
        case Rating.Teen:
            return 'Teen';
        case Rating.General:
            return 'General';
        default:
            return rating;
    }
}
