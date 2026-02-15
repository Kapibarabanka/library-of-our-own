import type { Ao3FicInfo, Fic, FicKey, UserFicKey } from '$lib/types/domain-models';

export function getUserFicKey(fic: Fic): UserFicKey {
    return {
        userId: fic.userId,
        ficId: fic.ao3Info.id,
        ficType: fic.ao3Info.ficType,
    };
}

export function getKeyFromFic(fic: Fic): FicKey {
    return getFicKeyFromInfo(fic.ao3Info);
}

export function getFicKeyFromInfo(ao3Info: Ao3FicInfo): FicKey {
    return {
        ficId: ao3Info.id,
        ficType: ao3Info.ficType,
    };
}
