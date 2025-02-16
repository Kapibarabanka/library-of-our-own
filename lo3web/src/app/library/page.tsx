import { userId } from '@/types/constants';
import { getAllFics } from '@/utils/api-functions';
import LibraryPage from './_components/LibraryPage/LibraryPage';

export default async function Library() {
    const page = await getAllFics(userId).catch(e => null);
    return <LibraryPage allCards={page?.cards ?? []}></LibraryPage>;
}
