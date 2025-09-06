<script lang="ts">
    import StatsPage from '@app/stats/StatsPage.svelte';
    import type { PageProps } from './$types';
    import { getTagStats } from '$api/stats.remote';
    let { data }: PageProps = $props();
</script>

<svelte:boundary>
    <StatsPage tagField={data.tagField} stats={await getTagStats(data.tagField)}></StatsPage>
    {#snippet pending()}
        <p>Loading fistatsc...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error: {JSON.stringify(error)}</p>
    {/snippet}
</svelte:boundary>

<style></style>
