import { FicsPage, FicsPageRequest } from '../types/api-models';
import { apiUrl } from '../types/constants';

export async function getDefaultPage(userId: string): Promise<FicsPage> {
    const ficsRequest = { userId, pageSize: 100, pageNumber: 0 } as FicsPageRequest;
    const response = await fetch(apiUrl + '/cards/fics-page', {
        method: 'post',
        body: JSON.stringify(ficsRequest),
        headers: { 'Content-Type': 'application/json' },
    });
    const data: FicsPage = await response.json();

    return data;
}

export async function getAllFics(userId: string): Promise<FicsPage> {
    const params = new URLSearchParams();
    params.append('userId', userId);
    const response = await fetch(apiUrl + `/cards/${userId}/all-fics`);
    const data: FicsPage = await response.json();

    return data;
}
