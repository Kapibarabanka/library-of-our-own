import { getUserCookie } from '$lib/utils/auth-utils.js';
import { redirect } from '@sveltejs/kit';

export function load({}) {
    const userCookie = getUserCookie();
    if (userCookie) {
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
