import { command } from '$app/server';
import { FinishInfoSchema } from '$lib/types/api-models';
import { FicDetailsSchema, UserFicKeySchema } from '$lib/types/domain-models';
import { patch, post } from '$api/api-utils';
import z from 'zod';

const base = 'fic-details';

export const finishFic = command(FinishInfoSchema, async finishInfo => {
    await post(base, 'finish-fic', finishInfo);
});

export const startedToday = command(UserFicKeySchema, async key => {
    await patch(base, 'started-today', key);
});

export const patchDetails = command(
    z.object({ key: UserFicKeySchema, details: FicDetailsSchema }),
    async ({ key, details }) => {
        await patch(base, 'patch-details', key, details);
    }
);
