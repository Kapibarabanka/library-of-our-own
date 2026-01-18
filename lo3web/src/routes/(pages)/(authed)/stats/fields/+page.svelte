<script lang="ts">
    import FieldStatsPage from '@app/stats/FieldStatsPage.svelte';
</script>

<svelte:boundary>
    <FieldStatsPage />
    {#snippet pending()}
        <p>Loading stats...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error in stats: {error}</p>
    {/snippet}
</svelte:boundary>

<style></style>
