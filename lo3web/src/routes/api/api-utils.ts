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

export async function post(controller: string, endpoint: string, params?: object, body?: unknown) {
    const resp = await fetch(getUrl(controller, endpoint, params), {
        method: 'POST',
        ...(body
            ? {
                  headers: {
                      Accept: 'application/json',
                      'Content-Type': 'application/json',
                  },
                  body: JSON.stringify(body),
              }
            : {}),
    });
    if (!resp.ok) {
        const message = resp.bodyUsed ? JSON.stringify(await resp.json()) : resp.statusText;
        console.log(resp);
        error(resp.status, message);
    }
    return resp.json();
}

export async function patch(controller: string, endpoint: string, params?: object, body?: object) {
    const resp = await fetch(getUrl(controller, endpoint, params), {
        method: 'PATCH',
        ...(body
            ? {
                  headers: {
                      Accept: 'application/json',
                      'Content-Type': 'application/json',
                  },
                  body: JSON.stringify(body),
              }
            : {}),
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
