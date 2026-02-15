<script lang="ts">
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import { UserImpression } from '$lib/types/domain-models';
    import Label from '$ui/label/label.svelte';
    import { filterState } from '../state.svelte';
</script>

<div class="flex flex-col gap-1">
    <Label class="text-muted-foreground">Click on impression to include it</Label>
    <div class="flex flex-wrap gap-y-1 justify-around">
        {#each Object.values(UserImpression) as impression}
            <ImpressionBadge
                {impression}
                withLabel={true}
                onclick={() => filterState.allowedImpressions.add(impression)}
            ></ImpressionBadge>
        {/each}
    </div>
</div>

<style>
</style>
