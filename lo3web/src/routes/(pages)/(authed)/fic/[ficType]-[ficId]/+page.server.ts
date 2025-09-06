import { FicType, type FicKey } from '$lib/types/domain-models.js';
import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const ssr = false;
export const load: PageServerLoad = async ({ params }) => {
    const ficType: FicType | null =
        params.ficType === 'work' ? FicType.Work : params.ficType === 'series' ? FicType.Series : null;
    const ficId = params.ficId;
    if (!ficType || !ficId) error(404);

    const ficKey: FicKey = { ficId, ficType };

    return { ficKey };
};
