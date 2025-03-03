import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';
import type { FicCardData } from '$lib/types/domain-models';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const allFics: Promise<FicCardData[]> = fetch(`${PUBLIC_API}/fics/${PUBLIC_USER}/all-cards`).then(response =>
        response.json()
    );
    return { allFics };
};
