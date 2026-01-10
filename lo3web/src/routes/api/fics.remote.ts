import { command, query } from '$app/server';
import { getUser } from '$lib/utils/auth-utils';
import { get, post, tryGet } from './api-utils';
import type { HomePageData } from '$lib/types/api-models';
import { FicKeySchema, type Ao3FicInfo, type Fic, type FicCardData, type UserFicKey } from '$lib/types/domain-models';
import z from 'zod';
import { ErrorType } from './errors-utils';

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

export const getFicByLink = query(z.string(), async link => {
    const user = getUser();
    const request = {
        ficLink: link,
        userId: user.id,
        needToLog: false,
    };
    const response = await tryGet(base, 'fic-by-link', [ErrorType.NotAo3Link, ErrorType.RestrictedWork], request);
    return response.result ? { fic: response.result as Fic, error: null } : { fic: null, error: response.error };
});

export const updateAo3Info = command(FicKeySchema, async key => {
    const user = getUser();
    const userFicKey: UserFicKey = {
        userId: user.id,
        ...key,
    };
    return post(base, 'update-ao3-info', { ...userFicKey, needToLog: false }) as Promise<Ao3FicInfo>;
});
