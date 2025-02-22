<script lang="ts">
    import type { FicCardData } from '$lib/types/domain-models';
    import FicCard from './FicCard.svelte';
    import { pageState } from './state.svelte';
    import Autocomplete from '@smui-extra/autocomplete';

    let fruits = ['Apple', 'Orange', 'Banana', 'Mango'];

    let valueStandard: string | undefined = $state();

    let { inputCards }: { inputCards: FicCardData[] } = $props();
    pageState.allCards = inputCards;
</script>

<div class="flex flex-col gap-2 p-2">
    <button onclick={() => (pageState.appliedFilters.isSpicy = true)}>Spicy: Yes</button>
    <button onclick={() => (pageState.appliedFilters.isSpicy = false)}>Spicy: No</button>
    <button onclick={() => (pageState.appliedFilters.isSpicy = undefined)}>Spicy: undefined</button>
    <div>
        <Autocomplete options={fruits} bind:value={valueStandard} label="Standard" />

        <pre class="status">Selected: {valueStandard || ''}</pre>
    </div>
    <div class="flex flex-col gap-1">
        <!-- {filtersState.appliedFilters.HasFilter ? (
            <span>Filtered results ({displayedCards.length}):</span>
        ) : null} -->
        <div>Total: {pageState.filteredCards.length}</div>
        <div class="flex flex-col gap-2">
            {#each pageState.filteredCards as cardData}
                <FicCard {cardData}></FicCard>
            {/each}
        </div>
    </div>
</div>

<style></style>
