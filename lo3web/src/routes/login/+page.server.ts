import type { UserCookie } from '$lib/types/ui-models.js';
import { redirect } from '@sveltejs/kit';

export function load({ cookies, url }) {
    const hash = url.searchParams.get('hash');
    const id = url.searchParams.get('id');
    if (hash && id) {
        // todo add hash check
        const userCookie: UserCookie = {
            id,
            hash,
            first_name: url.searchParams.get('first_name'),
            last_name: url.searchParams.get('last_name'),
            username: url.searchParams.get('username'),
            photo_url: url.searchParams.get('photo_url'),
            auth_date: url.searchParams.get('auth_date'),
        };
        cookies.set('logged_user', JSON.stringify(userCookie), { path: '/', secure:false });
        redirect(303, '/home');
    }
}
