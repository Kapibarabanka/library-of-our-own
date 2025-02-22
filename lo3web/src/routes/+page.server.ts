import type { FicsPage } from '$lib/types/api-models';
import type { PageLoad } from './$types';
import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';

export const load: PageLoad = async () => {
    const page: Promise<FicsPage> = fetch(`${PUBLIC_API}/cards/${PUBLIC_USER}/all-fics`).then(response =>
        response.json()
    );
    return { page };
};
