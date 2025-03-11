import { browser } from '$app/environment';
import { PUBLIC_CLIENT_API, PUBLIC_SERVER_API } from '$env/static/public';

// todo need to figure out a normal way to do this, or just use all on server/all on client
export function getBaseUrl() {
    return browser ? PUBLIC_CLIENT_API : PUBLIC_SERVER_API;
}
