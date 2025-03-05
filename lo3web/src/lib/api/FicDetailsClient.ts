import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';
import type { FicType } from '$lib/types/domain-models';

const prefix = `${PUBLIC_API}/fic-details`;

export default class FicDetailsClient {
    public static startedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(`${prefix}/started-today` + new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }), {
            method: 'PATCH',
        }).then(() => {});
    }
    public static finishedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(`${prefix}/finished-today` + new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }), {
            method: 'PATCH',
        }).then(() => {});
    }
    public static abandonedToday(ficId: string, ficType: FicType): Promise<void> {
        return fetch(`${prefix}/abandoned-today` + new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType }), {
            method: 'PATCH',
        }).then(() => {});
    }
}
