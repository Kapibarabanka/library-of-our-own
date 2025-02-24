<script lang="ts">
    import type { FicCardData } from '$lib/types/domain-models';
    import Label from '$ui/label/label.svelte';
    import FicCard from './FicCard.svelte';
    import FiltersHeader from './FiltersHeader.svelte';
    import { pageState } from './state.svelte';

    let { inputCards }: { inputCards: FicCardData[] } = $props();
    pageState.allCards = inputCards;
</script>

<div class="flex flex-col gap-2 p-2">
    <FiltersHeader></FiltersHeader>
    <div class="flex flex-col gap-1">
        <Label for="fics">{pageState.hasApplied ? 'Filtered fics' : 'Total'}: {pageState.filteredCards.length}</Label>
        <div id="fics" class="flex flex-col gap-2">
            {#each pageState.filteredCards as cardData}
                <FicCard {cardData}></FicCard>
            {/each}
        </div>
    </div>
</div>

<style></style>
