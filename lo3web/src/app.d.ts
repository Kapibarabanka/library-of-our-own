// See https://svelte.dev/docs/kit/types#app.d.ts

import type { UserCookie } from '$lib/types/ui-models';

// for information about these interfaces
declare global {
    namespace App {
        // interface Error {}
        interface Locals {
            userCookie: UserCookie | null;
        }
        // interface PageData {}
        // interface PageState {}
        // interface Platform {}
    }
}

export {};
