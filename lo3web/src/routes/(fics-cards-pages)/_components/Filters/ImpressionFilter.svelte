<script lang="ts">
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import { UserImpression } from '$lib/types/domain-models';
    import Label from '$ui/label/label.svelte';
    import { pageState } from '../state.svelte';
</script>

<Label class="text-muted-foreground">Click on impression to include it</Label>
<div class="flex justify-between">
    {#each Object.values(UserImpression) as impression}
        <ImpressionBadge
            {impression}
            withLabel={true}
            onclick={() => pageState.appliedFilters.allowedImpressions.add(impression)}
        ></ImpressionBadge>
    {/each}
</div>

<style>
</style>
