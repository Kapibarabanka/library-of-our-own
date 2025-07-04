import type { UserCookie } from '$lib/types/ui-models.js';
import { cookieIsValid } from '$lib/utils/auth-utils.js';
import { redirect } from '@sveltejs/kit';

export function load({ cookies }) {
    const cookie = cookies.get('logged_user');
    if (!cookie) {
        redirect(303, '/login');
    }
    const userCookie: UserCookie = JSON.parse(cookie);
    if (!!cookie && cookieIsValid(userCookie)) {
        const user = {
            id: userCookie.id,
            name: userCookie.username ?? [userCookie.first_name, userCookie.last_name].join(' '),
            photoUrl: userCookie.photo_url,
        };
        return { user };
    } else {
        redirect(303, '/login');
    }
}
