import type { FicsPage } from '$lib/types/api-models';
import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const page: Promise<FicsPage> = fetch(`${PUBLIC_API}/cards/${PUBLIC_USER}/all-fics`).then(response =>
        response.json()
    );
    return { page };
};
