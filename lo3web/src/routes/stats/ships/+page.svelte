<script lang="ts">
    import StatsPage from '@app/stats/StatsPage.svelte';
    import type { PageProps } from './$types';
    let { data }: PageProps = $props();
</script>

{#await data.stats}
    <p>Loading stats...</p>
{:then stats}
    <StatsPage tagField={data.tagField} {stats}></StatsPage>
{:catch error}
    <p>Error {error.message}</p>
{/await}

<style></style>
