<script lang="ts">
    import LibraryPage from '@app/library/LibraryPage.svelte';
    import { getAllCards } from '$api/fics.remote';
</script>

<svelte:boundary>
    <LibraryPage inputCards={await getAllCards()}></LibraryPage>
    {#snippet pending()}
        <p>Loading fics...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error: {JSON.stringify(error)}</p>
    {/snippet}
</svelte:boundary>
