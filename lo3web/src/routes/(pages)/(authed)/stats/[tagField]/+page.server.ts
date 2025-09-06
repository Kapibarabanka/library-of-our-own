import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { StatTagField } from '$lib/types/api-models';

export const ssr = false;
export const load: PageServerLoad = async ({ params }) => {
    const tagField = toEnum(params.tagField);
    return { tagField };
};

function toEnum(field: string): StatTagField {
    switch (field) {
        case 'fandoms':
            return StatTagField.Fandom;
        case 'ships':
            return StatTagField.Ship;
        case 'tags':
            return StatTagField.Tag;
        default:
            error(404);
    }
}
