import { FicType } from '$lib/types/domain-models.js';
import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const ssr = false;
export const load: PageServerLoad = async ({ params }) => {
    const ficType = params.ficType === 'work' ? FicType.Work : params.ficType === 'series' ? FicType.Series : null;
    if (!ficType) error(404);
    const id = params.ficId;

    return { ficType, id };
};
