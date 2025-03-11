import { PUBLIC_USER } from '$env/static/public';
import type { HomePageData } from '$lib/types/api-models';
import type { Fic, FicCardData, FicType } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = 'fics';

export default class FicsClient {
    public static getHomePage(): Promise<HomePageData> {
        return fetch(`${getBaseUrl()}/${controller}/${PUBLIC_USER}/home-page`).then(response => response.json());
    }

    public static getAllCards(): Promise<FicCardData[]> {
        return fetch(`${getBaseUrl()}/${controller}/${PUBLIC_USER}/all-cards`).then(response => response.json());
    }

    public static getFic(ficId: string, ficType: FicType): Promise<Fic> {
        return fetch(
            `${getBaseUrl()}/${controller}/fic-by-key?` + new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType })
        ).then(response => response.json());
    }
}
