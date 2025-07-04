import FicsClient from '$api/FicsClient';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ parent }) => {
    const { user } = await parent();
    const allFics = FicsClient.getAllCards(user.id);
    return { allFics };
};
