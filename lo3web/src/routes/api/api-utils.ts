import { PUBLIC_SERVER_API } from '$env/static/public';
import { ensureAuth } from '$lib/utils/auth-utils';
import { error } from '@sveltejs/kit';
import { asRecoverableError, ErrorType, type RecoverableError } from './errors-utils';

export async function tryGet(
    controller: string,
    endpoint: string,
    expectedErrors: ErrorType[],
    params?: object
): Promise<{ result: any; error: RecoverableError | null }> {
    const resp = await fetch(getUrl(controller, endpoint, params));
    if (!resp.ok) {
        const respClone = resp.clone();
        const recoverableError = await asRecoverableError(resp, expectedErrors);
        if (recoverableError == null) {
            error(resp.status, await respClone.text());
        }
        return { result: null, error: recoverableError };
    } else {
        return { result: await resp.json(), error: null };
    }
}

export async function get(controller: string, endpoint: string, params?: object) {
    const resp = await fetch(getUrl(controller, endpoint, params));
    if (!resp.ok) {
        error(resp.status, await resp.text());
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
        error(resp.status, await resp.text());
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
        error(resp.status, await resp.text());
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
