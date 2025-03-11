import { PUBLIC_USER } from '$env/static/public';
import type { FinishInfo } from '$lib/types/api-models';
import type { FicType } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = 'fic-details';

export default class FicDetailsClient {
    public static startedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(
            `${getBaseUrl()}/${controller}/started-today?` +
                new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }),
            {
                method: 'PATCH',
            }
        ).then(() => {});
    }
    public static finishedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(
            `${getBaseUrl()}/${controller}/finished-today?` +
                new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }),
            {
                method: 'PATCH',
            }
        ).then(() => {});
    }
    public static abandonedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(
            `${getBaseUrl()}/${controller}/abandoned-today?` +
                new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }),
            {
                method: 'PATCH',
            }
        ).then(() => {});
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
