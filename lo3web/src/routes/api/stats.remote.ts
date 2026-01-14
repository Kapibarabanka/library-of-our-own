import { query } from '$app/server';
import { StatTagFieldSchema, type TagFieldStats } from '$lib/types/api-models';
import { checkAuthAndGetUserId } from '$lib/utils/auth-utils';
import { get } from './api-utils';

const controller = 'stats';

export const getTagFieldStats = query(StatTagFieldSchema, async tagField => {
    const userId = checkAuthAndGetUserId();
    return get(controller, `${userId}/stats/${tagField}`) as Promise<TagFieldStats>;
});
