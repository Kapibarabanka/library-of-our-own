import { tryGetUserCookie } from '$lib/utils/auth-utils.js';
import { redirect } from '@sveltejs/kit';

export function load() {
    const userCookie = tryGetUserCookie();
    if (!userCookie) {
        redirect(303, '/login');
    }
}
