import { command, query } from '$app/server';
import { checkAuthAndGetUserId } from '$lib/utils/auth-utils';
import z from 'zod';
import { get, patch } from './api-utils';

const base = 'user';

export const getKindleEmail = query(async () => {
    const userId = checkAuthAndGetUserId();
    const res = await (get(base, `${userId}/email`) as Promise<string>);
    return res;
});

export const setKindleEmail = command(z.string().optional(), async email => {
    const userId = checkAuthAndGetUserId();
    await patch(base, `${userId}/email`, { email });
});
