<script lang="ts">
    import LibraryPage from '@app/library/LibraryPage.svelte';
    import type { PageProps } from './$types';
    let { data }: PageProps = $props();
</script>

{#await data.allFics}
    <p>Loading fics...</p>
{:then allCards}
    <LibraryPage inputCards={allCards}></LibraryPage>
{:catch error}
    <p>Error {error.message}</p>
{/await}
