import { redirect } from '@sveltejs/kit';
export function load({ cookies }) {
    cookies.delete('logged_user', { path: '/' });
    redirect(303, '/login');
}
