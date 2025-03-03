import { FicType, type Fic } from '$lib/types/domain-models.js';
import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';

export const ssr = false;
export const load: PageServerLoad = async ({ params }) => {
    const ficType = params.ficType === 'work' ? FicType.Work : params.ficType === 'series' ? FicType.Series : null;
    if (!ficType) error(404);
    const ficId = params.ficId;

    const fic: Promise<Fic> = fetch(
        `${PUBLIC_API}/fics/fic-by-key?` + new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType })
    ).then(response => response.json());

    return { fic };
};
