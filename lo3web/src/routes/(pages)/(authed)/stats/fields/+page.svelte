<script lang="ts">
    import FieldStats from '@app/stats/FieldStats.svelte';
</script>

<svelte:boundary>
    <FieldStats />
    {#snippet pending()}
        <p>Loading stats...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error in stats: {error}</p>
    {/snippet}
</svelte:boundary>

<style></style>
