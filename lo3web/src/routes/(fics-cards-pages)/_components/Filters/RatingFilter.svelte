<script lang="ts">
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import { Rating } from '$lib/types/domain-models';
    import Label from '$ui/label/label.svelte';
    import { pageState } from '../state.svelte';
</script>

<Label class="text-muted-foreground">Click on rating to include it</Label>
<div class="flex flex-wrap justify-between">
    {#each Object.values(Rating) as rating}
        <RatingIcon {rating} variant={'short'} onclick={() => pageState.appliedFilters.allowedRatings.add(rating)}
        ></RatingIcon>
    {/each}
</div>

<style>
</style>
