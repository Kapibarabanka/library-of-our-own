import { PUBLIC_API, PUBLIC_USER } from '$env/static/public';
import type { FicType } from '$lib/types/domain-models';

const prefix = `${PUBLIC_API}/kindle`;

export default class KindleClient {
    public static async sendToKindle(ficId: string, ficType: FicType): Promise<void> {
        await fetch(
            `${prefix}/send-to-kindle?` +
                new URLSearchParams({ userId: PUBLIC_USER, ficId, ficType, needToLog: 'false' }),
            { method: 'POST' }
        );
    }
}
