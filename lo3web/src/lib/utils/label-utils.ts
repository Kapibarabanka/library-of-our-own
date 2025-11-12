import { Rating, UserImpression } from '$lib/types/domain-models';
import moment from 'moment';

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
export function shortImpression(impression: UserImpression) {
    switch (impression) {
        case UserImpression.Never:
            return 'Never';
        default:
            return impression;
    }
}

export function formatDate(date: string) {
    return moment(date).format('D MMM, YYYY');
}

export function formatDateTime(date: string) {
    return moment(date).format('D MMM, YYYY, HH:mm');
}
