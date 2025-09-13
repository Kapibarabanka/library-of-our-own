import { query } from '$app/server';
import { StatTagFieldSchema, type TagFieldStats } from '$lib/types/api-models';
import { getUser } from '$lib/utils/auth-utils';
import { get } from './api-utils';

const controller = 'stats';

export const getTagFieldStats = query(StatTagFieldSchema, async tagField => {
    const user = getUser();
    return get(controller, `${user.id}/stats/${tagField}`) as Promise<TagFieldStats>;
});
