<script lang="ts">
    import type { PageProps } from './$types';

    let { data }: PageProps = $props();
</script>

{#await data.homePage}
    <p>Loading home page...</p>
{:then homePage}
    <div>Currently reading</div>
    {#each homePage.currentlyReading as startedFic}
        <div><a href={startedFic.ao3Info.link}>{startedFic.ao3Info.title}</a></div>
    {/each}
    <div>Random fic</div>
    <p>{homePage.randomFicFromBacklog?.ao3Info.title ?? 'No fics in backlog'}</p>
{:catch error}
    <p>Error {error.message}</p>
{/await}
