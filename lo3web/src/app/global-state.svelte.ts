import type { User } from '$lib/types/ui-models';

export class GlobalState {
    public user: User | undefined = $state();
}

export const globalState = new GlobalState();
