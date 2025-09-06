import { query } from '$app/server';
import { getUser } from '$lib/utils/auth-utils';
import { get } from './api-utils';
import type { HomePageData } from '$lib/types/api-models';
import { FicKeySchema, type Fic, type FicCardData, type UserFicKey } from '$lib/types/domain-models';

const base = 'fics';

export const getHomePage = query(async () => {
    const user = getUser();
    const res = await (get(base, `${user.id}/home-page`) as Promise<HomePageData>); // todo maybe use zod for parsing
    return res;
});

export const getAllCards = query(async () => {
    const user = getUser();
    return get(base, `${user.id}/all-cards`) as Promise<FicCardData[]>; // todo maybe use zod for parsing
});

export const getFic = query(FicKeySchema, async key => {
    const user = getUser();
    const userFicKey: UserFicKey = {
        userId: user.id,
        ...key,
    };
    return get(base, 'fic-by-key', userFicKey) as Promise<Fic>; // todo maybe use zod for parsing
});
