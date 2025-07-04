import type { PageServerLoad } from './$types';
import StatsClient from '$api/StatsClient';
import { StatTagField } from '$lib/types/api-models';

export const ssr = false;
export const load: PageServerLoad = async ({ parent }) => {
    const { user } = await parent();
    const tagField = StatTagField.Ship;
    const stats = StatsClient.getTagStats(user.id, tagField);
    return { tagField, stats };
};
