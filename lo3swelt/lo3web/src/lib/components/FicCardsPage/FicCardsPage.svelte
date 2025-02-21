<script lang="ts">
	import type { FicCardData } from '$lib/types/domain-models';
	import FicCard from './FicCard.svelte';
	import { page } from './state.svelte';
	let { inputCards }: { inputCards: FicCardData[] } = $props();
	page.allCards = inputCards;
</script>

<!-- Todo replace with filtered cards (derived state) -->

<div class="flex flex-col gap-2 p-2">
	<!-- <Filters filtersState={filtersState} onAppliedChanged={handleFilterChange}></Filters> -->
	<div class="flex flex-col gap-1">
		<!-- {filtersState.appliedFilters.HasFilter ? (
            <span>Filtered results ({displayedCards.length}):</span>
        ) : null} -->
		<div class="flex flex-col gap-2">
			{#each page.allCards as cardData}
				<FicCard {cardData}></FicCard>
			{/each}
		</div>
	</div>
</div>

<style></style>
