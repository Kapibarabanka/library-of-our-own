import { PUBLIC_USER } from '$env/static/public';
import type { MonthStats } from '$lib/types/api-models';
import { getBaseUrl } from './api-utils';

const controller = 'stats';

export default class StatsClient {
    // public static getGeneralStats(): Promise<MonthStats[]> {
    //     return fetch(`${getBaseUrl()}/${controller}/${PUBLIC_USER}/general-stats`).then(response => response.json());
    // }
}
