import { PUBLIC_USER } from '$env/static/public';
import type { FicType } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = `kindle`;

export default class KindleClient {
    public static async sendToKindle(ficId: string, ficType: FicType): Promise<void> {
        await fetch(
            `${getBaseUrl()}/${controller}/send-to-kindle?` +
                new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType, needToLog: 'false' }),
            { method: 'POST' }
        );
    }
}
