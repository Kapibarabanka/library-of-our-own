import FicsClient from '$api/FicsClient';
import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const homePage = FicsClient.getHomePage();
    return { homePage };
};

export const actions = {
    abandon: async event => {
        // TODO log the user in
    },
    finish: async ({ cookies, request }) => {
        const data = await request.formData();
        const ficType = data.get('ficType');
        const ficId = data.get('ficId');

        // db.deleteTodo(cookies.get('userid'), data.get('id'));
    },
} satisfies Actions;
