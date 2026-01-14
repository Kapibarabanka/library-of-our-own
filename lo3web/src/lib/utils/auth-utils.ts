import type { UserCookie } from '$lib/types/ui-models';
import { MAIN_BOT } from '$env/static/private';
import * as crypto from 'crypto';
import { getRequestEvent } from '$app/server';
import { error } from '@sveltejs/kit';

export const userCookieName = 'logged_user';

export function cookieIsValid(cookie: UserCookie) {
    const hash = cookie.hash;
    const cookieCopy = { ...cookie };
    delete cookieCopy.hash;
    const dataCheckString = Object.entries(cookieCopy)
        .filter(([, val]) => !!val)
        .map(([key, val]) => `${key}=${val}`)
        .sort()
        .join('\n');
    const key = crypto.createHash('sha256').update(MAIN_BOT).digest();
    const hashCheck = crypto.createHmac('sha256', key).update(dataCheckString).digest('hex');
    return hash === hashCheck;
}

export function tryGetUserCookie() {
    const { cookies } = getRequestEvent();
    const cookie = cookies.get(userCookieName);
    return tryParseCookie(cookie);
}

export function checkAuthAndGetUserId(): string {
    const maybeCookie = tryGetUserCookie();
    if (maybeCookie) {
        return maybeCookie.id;
    } else {
        error(401);
    }
}
export function ensureAuth(): void {
    const maybeCookie = tryGetUserCookie();
    if (!maybeCookie) {
        error(401);
    }
}

function tryParseCookie(cookie: string | undefined) {
    if (!cookie) {
        return null;
    }
    const userCookie: UserCookie = JSON.parse(cookie);
    return cookieIsValid(userCookie) ? userCookie : null;
}
