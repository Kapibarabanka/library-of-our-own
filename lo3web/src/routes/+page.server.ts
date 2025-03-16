import FicsClient from '$api/FicsClient';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const homePage = FicsClient.getHomePage();
    return { homePage };
};
