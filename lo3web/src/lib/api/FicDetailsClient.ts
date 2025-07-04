import type { FinishInfo } from '$lib/types/api-models';
import type { UserFicKey } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = 'fic-details';

export default class FicDetailsClient {
    public static startedToday(ficKey: UserFicKey): Promise<void> {
        return fetch(`${getBaseUrl()}/${controller}/started-today?` + new URLSearchParams({ ...ficKey }), {
            method: 'PATCH',
        }).then(() => {});
    }
    public static finishedToday(ficKey: UserFicKey): Promise<void> {
        return fetch(`${getBaseUrl()}/${controller}/finished-today?` + new URLSearchParams({ ...ficKey }), {
            method: 'PATCH',
        }).then(() => {});
    }
    public static abandonedToday(ficKey: UserFicKey): Promise<void> {
        return fetch(`${getBaseUrl()}/${controller}/abandoned-today?` + new URLSearchParams({ ...ficKey }), {
            method: 'PATCH',
        }).then(() => {});
    }
    public static finishFic(data: FinishInfo): Promise<void> {
        return fetch(`${getBaseUrl()}/${controller}/finish-fic`, {
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST',
            body: JSON.stringify(data),
        })
            .catch(e => console.log(e))
            .then(() => {});
    }
}
