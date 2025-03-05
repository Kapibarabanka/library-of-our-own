<script lang="ts">
    import type { HomePageData } from '$lib/types/api-models';

    let { homePage }: { homePage: HomePageData } = $props();
</script>

<div>Currently reading</div>
{#each homePage.currentlyReading as startedFic}
    <div><a href={startedFic.ao3Info.link}>{startedFic.ao3Info.title}</a></div>
{/each}
<div>Random fic</div>
<p>{homePage.randomFicFromBacklog?.ao3Info.title ?? 'No fics in backlog'}</p>

<style></style>
