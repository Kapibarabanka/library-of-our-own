import FicsClient from '$api/FicsClient';
import type { PageServerLoad } from './$types';

export const ssr = false;
export const load: PageServerLoad = async ({ parent }) => {
    const { user } = await parent();
    const homePage = FicsClient.getHomePage(user.id);
    return { homePage };
};
