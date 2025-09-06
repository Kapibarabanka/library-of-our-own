import { PUBLIC_SERVER_API } from '$env/static/public';
import { ensureAuth } from '$lib/utils/auth-utils';
import { error } from '@sveltejs/kit';

export async function get(controller: string, endpoint: string, params?: object) {
    const resp = await fetch(getUrl(controller, endpoint, params));
    if (!resp.ok) {
        const message = resp.bodyUsed ? JSON.stringify(await resp.json()) : resp.statusText;
        error(resp.status, message);
    } else {
        return resp.json();
    }
}

export async function post(controller: string, endpoint: string, data: unknown) {
    const resp = await fetch(getUrl(controller, endpoint), {
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
        },
        method: 'POST',
        body: JSON.stringify(data),
    });
    if (!resp.ok) {
        const message = resp.bodyUsed ? JSON.stringify(await resp.json()) : resp.statusText;
        error(resp.status, message);
    }
}

export async function patch(controller: string, endpoint: string, params?: object) {
    const resp = await fetch(getUrl(controller, endpoint, params), {
        method: 'PATCH',
    });
    if (!resp.ok) {
        const message = resp.bodyUsed ? JSON.stringify(await resp.json()) : resp.statusText;
        error(resp.status, message);
    }
}

function getUrl(controller: string, endpoint: string, params?: object) {
    ensureAuth();
    let url = `${PUBLIC_SERVER_API}/${controller}/${endpoint}`;
    if (params) {
        url = url + '?' + new URLSearchParams({ ...params });
    }
    return url;
}
