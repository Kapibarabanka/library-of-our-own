import type { HomePageData } from '$lib/types/api-models';
import type { Fic, FicCardData, UserFicKey } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = 'fics';

export default class FicsClient {
    public static getHomePage(userId: string): Promise<HomePageData> {
        return fetch(`${getBaseUrl()}/${controller}/${userId}/home-page`).then(response => response.json());
    }

    public static getAllCards(userId: string): Promise<FicCardData[]> {
        return fetch(`${getBaseUrl()}/${controller}/${userId}/all-cards`).then(response => response.json());
    }

    public static getFic(ficKey: UserFicKey): Promise<Fic> {
        return fetch(`${getBaseUrl()}/${controller}/fic-by-key?` + new URLSearchParams({ ...ficKey })).then(response =>
            response.json()
        );
    }
}
