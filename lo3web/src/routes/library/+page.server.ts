import FicsClient from '$api/FicsClient';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const allFics = FicsClient.getAllCards();
    return { allFics };
};
