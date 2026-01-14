import { getKindleEmail } from '$api/user.remote';
import type { User } from '$lib/types/ui-models';
import { tryGetUserCookie } from '$lib/utils/auth-utils.js';
import { redirect } from '@sveltejs/kit';

export async function load() {
    const userCookie = tryGetUserCookie();
    if (!userCookie) {
        redirect(303, '/login');
    }

    const kindleEmail = await getKindleEmail();

    const user: User = {
        id: userCookie.id,
        name: userCookie.username ?? [userCookie.first_name, userCookie.last_name].join(' '),
        photoUrl: userCookie.photo_url,
        kindleEmail: kindleEmail,
    };
    return {
        user,
    };
}
