import type { UserCookie } from '$lib/types/ui-models';
import { MAIN_BOT } from '$env/static/private';
import * as crypto from 'crypto';

export function cookieIsValid(cookie: UserCookie) {
    const hash = cookie.hash;
    delete cookie.hash;
    const dataCheckString = Object.entries(cookie)
        .filter(([, val]) => !!val)
        .map(([key, val]) => `${key}=${val}`)
        .sort()
        .join('\n');
    const key = crypto.createHash('sha256').update(MAIN_BOT).digest();
    const hashCheck = crypto.createHmac('sha256', key).update(dataCheckString).digest('hex');
    return hash === hashCheck;
}
