import type { UserFicKey } from '$lib/types/domain-models';
import { getBaseUrl } from './api-utils';

const controller = `kindle`;

export default class KindleClient {
    public static async sendToKindle(ficKey: UserFicKey): Promise<void> {
        await fetch(
            `${getBaseUrl()}/${controller}/send-to-kindle?` + new URLSearchParams({ ...ficKey, needToLog: 'false' }),
            { method: 'POST' }
        );
    }
}
