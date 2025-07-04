import type { StatTagField, TagFieldStats } from '$lib/types/api-models';
import { getBaseUrl } from './api-utils';

const controller = 'stats';

export default class StatsClient {
    public static getTagStats(userId: string, tagField: StatTagField): Promise<TagFieldStats> {
        return fetch(`${getBaseUrl()}/${controller}/${userId}/${tagField}/stats`).then(response => response.json());
    }
}
