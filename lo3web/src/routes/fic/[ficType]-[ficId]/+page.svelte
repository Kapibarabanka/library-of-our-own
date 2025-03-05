<script lang="ts">
    import FicPage from '@app/fic/FicPage.svelte';
    import type { PageProps } from './$types';

    let { data }: PageProps = $props();
</script>

{#await data.fic}
    <p>Loading fic...</p>
{:then fic}
    <FicPage {fic}></FicPage>
{:catch error}
    <p>Error {error.message}</p>
{/await}
