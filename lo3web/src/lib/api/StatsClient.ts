import { PUBLIC_USER } from '$env/static/public';
import type { StatTagField, TagFieldStats } from '$lib/types/api-models';
import { getBaseUrl } from './api-utils';

const controller = 'stats';

export default class StatsClient {
    public static getTagStats(tagField: StatTagField): Promise<TagFieldStats> {
        return fetch(`${getBaseUrl()}/${controller}/${PUBLIC_USER}/${tagField}/stats`).then(response =>
            response.json()
        );
    }
}
