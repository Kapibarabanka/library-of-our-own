import type { PageServerLoad } from './$types';
import StatsClient from '$api/StatsClient';
import { StatTagField } from '$lib/types/api-models';

export const ssr = false;
export const load: PageServerLoad = async () => {
    const tagField = StatTagField.Fandom;
    const stats = StatsClient.getTagStats(tagField);
    return { tagField, stats };
};
