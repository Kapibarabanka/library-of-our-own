<script lang="ts">
    import FicPage from '@app/fic/FicPage.svelte';
    import type { PageProps } from './$types';
    import { getFic } from '$api/fics.remote';

    let { data }: PageProps = $props();
</script>

<svelte:boundary>
    <FicPage fic={await getFic(data.ficKey)}></FicPage>
    {#snippet pending()}
        <p>Loading fic...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error: {error}</p>
    {/snippet}
</svelte:boundary>
