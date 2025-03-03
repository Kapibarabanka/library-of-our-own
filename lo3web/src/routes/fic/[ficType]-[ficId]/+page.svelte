<script lang="ts">
    import type { PageProps } from './$types';

    let { data }: PageProps = $props();
</script>

{#await data.fic}
    <p>Loading fic...</p>
{:then fic}
    <div>Fic with title '{fic.ao3Info.title}'</div>
{:catch error}
    <p>Error {error.message}</p>
{/await}
