<script lang="ts">
    import type { FicCardData, FicDetails } from '$lib/types/domain-models';
    import Label from '$ui/label/label.svelte';
    import FicCard from './FicCard.svelte';
    import FiltersHeader from './FiltersHeader.svelte';
    import { pageState } from './state.svelte';
    import * as Tabs from '$lib/components/ui/tabs';
    import SortHeader from './SortHeader.svelte';
    import HeaderCard from './HeaderCard.svelte';

    let { inputCards }: { inputCards: FicCardData[] } = $props();
    pageState.allCards = inputCards;

    function onPatchedDetails(patchedDetails: FicDetails, cardData: FicCardData) {
        cardData.details = patchedDetails;
    }
</script>

<div class="flex flex-col gap-3">
    <Tabs.Root value="filter" class="w-full">
        <Tabs.List class="grid  grid-cols-2 max-w-[500px]">
            <Tabs.Trigger value="filter">Filter</Tabs.Trigger>
            <Tabs.Trigger value="sort">Sort</Tabs.Trigger>
        </Tabs.List>
        <Tabs.Content value="filter">
            <HeaderCard title="Filter by">
                <FiltersHeader></FiltersHeader>
            </HeaderCard>
        </Tabs.Content>
        <Tabs.Content value="sort">
            <HeaderCard title="Sort by">
                <SortHeader></SortHeader>
            </HeaderCard>
        </Tabs.Content>
    </Tabs.Root>
    <div class="flex flex-col">
        <Label for="fics" class="text-center text-sm font-bold text-muted-foreground">
            Found {pageState.filteredCards.length} fics
        </Label>
        <div id="fics" class="flex flex-col gap-2">
            {#each pageState.filteredCards as cardData (cardData.key)}
                <FicCard {cardData} onPatchedDetails={details => onPatchedDetails(details, cardData)}></FicCard>
            {/each}
        </div>
    </div>
</div>

<style></style>
