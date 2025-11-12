import { command } from '$app/server';
import { UserFicKeySchema } from '$lib/types/domain-models';
import { post } from './api-utils';

const controller = 'kindle';

export const sendToKindle = command(UserFicKeySchema, async key => {
    await post(controller, 'send-to-kindle', { ...key, needToLog: false });
});
