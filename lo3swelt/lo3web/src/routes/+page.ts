import type { FicsPage } from "$lib/types/api-models";
import { apiUrl, userId } from "$lib/types/constants";
import type { PageLoad } from "./$types";

export const load: PageLoad = async () => {
    const page: Promise<FicsPage> = fetch(`${apiUrl}/cards/${userId}/all-fics`).then(response => response.json());    
    return {page};
};