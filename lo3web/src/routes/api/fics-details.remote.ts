import { command } from '$app/server';
import { FinishInfoSchema } from '$lib/types/api-models';
import { UserFicKeySchema } from '$lib/types/domain-models';
import { patch, post } from '$api/api-utils';

const base = 'fic-details';

export const finishFic = command(FinishInfoSchema, async finishInfo => {
    await post(base, 'finish-fic', finishInfo);
});

export const startedToday = command(UserFicKeySchema, async key => {
    await patch(base, 'started-today', key);
});
