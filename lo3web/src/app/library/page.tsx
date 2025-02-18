import { userId } from '@/types/constants';
import { getAllFics } from '@/utils/api-functions';
import LibraryPage from './_components/LibraryPage/LibraryPage';

export const dynamic = 'force-dynamic';

export default async function Library() {
    if (!userId) return <div>No user id was found</div>;
    const page = await getAllFics(userId).catch(() => null);
    if (!page) return <div>Failed to fetch library data</div>;
    return <LibraryPage allCards={page.cards}></LibraryPage>;
}
