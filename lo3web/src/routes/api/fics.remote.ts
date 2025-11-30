import { command, query } from '$app/server';
import { getUser } from '$lib/utils/auth-utils';
import { get, post } from './api-utils';
import type { HomePageData } from '$lib/types/api-models';
import { FicKeySchema, type Ao3FicInfo, type Fic, type FicCardData, type UserFicKey } from '$lib/types/domain-models';

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

export const updateAo3Info = command(FicKeySchema, async key => {
    const user = getUser();
    const userFicKey: UserFicKey = {
        userId: user.id,
        ...key,
    };
    return post(base, 'update-ao3-info', { ...userFicKey, needToLog: false }) as Promise<Ao3FicInfo>;
});
