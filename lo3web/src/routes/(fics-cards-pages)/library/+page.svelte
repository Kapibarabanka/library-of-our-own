<script lang="ts">
    import FicCardsPage from '../_components/FicCardsPage.svelte';
    import type { PageProps } from './$types';
    let { data }: PageProps = $props();
</script>

{#await data.allFics}
    <p>Loading fics...</p>
{:then allCards}
    <FicCardsPage inputCards={allCards}></FicCardsPage>
{:catch error}
    <p>Error {error.message}</p>
{/await}
