import type { Fic, FicKey, UserFicKey } from '$lib/types/domain-models';

export function getUserFicKey(fic: Fic): UserFicKey {
    return {
        userId: fic.userId,
        ficId: fic.ao3Info.id,
        ficType: fic.ao3Info.ficType,
    };
}

export function getFicKey(fic: Fic): FicKey {
    return {
        ficId: fic.ao3Info.id,
        ficType: fic.ao3Info.ficType,
    };
}
