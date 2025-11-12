import { command } from '$app/server';
import { FinishInfoSchema } from '$lib/types/api-models';
import { FicDetailsSchema, UserFicKeySchema, type FicNote } from '$lib/types/domain-models';
import { patch, post } from '$api/api-utils';
import z from 'zod';
import moment from 'moment';

const base = 'fic-details';

export const finishFic = command(FinishInfoSchema, async finishInfo => {
    await post(base, 'finish-fic', undefined, finishInfo);
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

export const addNote = command(z.object({ key: UserFicKeySchema, text: z.string() }), async ({ key, text }) => {
    const note: FicNote = { date: moment().toISOString().slice(0, -1), text };
    await patch(base, 'add-note', key, note);
    return note;
});
