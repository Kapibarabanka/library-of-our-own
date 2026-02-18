<script lang="ts">
    import type { HomePageData } from '$lib/types/api-models';
    import FicCard from '@app/library/FicCard.svelte';
    import { type FicCardData, type FicDetails } from '$lib/types/domain-models';
    import StartedFicCard from './StartedFicCard.svelte';
    import * as Sheet from '$lib/components/ui/sheet';
    import GeneralStatsChart from './GeneralStatsChart.svelte';
    import { getHomePage } from '$api/fics.remote';
    import Separator from '$ui/separator/separator.svelte';
    import FinishForm from '$lib/components/FinishForm.svelte';
    import SectionHeader from '$lib/components/SectionHeader.svelte';

    let { homePage }: { homePage: HomePageData } = $props();

    let open = $state(false);

    let selectedCard = $state<FicCardData | undefined>(undefined);

    function onFinishedPressed(card: FicCardData) {
        selectedCard = card;
        open = true;
    }

    function onPatchedDetails(patchedDetails: FicDetails) {
        if (homePage.randomFicFromBacklog) {
            homePage.randomFicFromBacklog.details = patchedDetails;
        }
    }

    async function onSubmitted() {
        await getHomePage().refresh();
        open = false;
    }
</script>

<div class="flex flex-col gap-4">
    <div class="flex flex-col">
        <SectionHeader text="Currently Reading" />
        <div class="flex flex-col gap-2">
            {#each homePage.currentlyReading as fic}
                <StartedFicCard {fic} onFinish={fic => onFinishedPressed(fic)}></StartedFicCard>
            {/each}
        </div>
    </div>
    <div class="flex flex-col">
        <SectionHeader text="Something you wanted to read" />
        {#if homePage.randomFicFromBacklog}
            <FicCard cardData={homePage.randomFicFromBacklog} onPatchedDetails={details => onPatchedDetails(details)}
            ></FicCard>
        {:else}
            <p>No fics in the backlog yet</p>
        {/if}
    </div>
    <Separator />
    <GeneralStatsChart stats={homePage.generalStats}></GeneralStatsChart>
</div>
<Sheet.Root bind:open>
    {#if selectedCard}
        {#key selectedCard.key}
            <FinishForm key={selectedCard.key} details={selectedCard.details} {onSubmitted}></FinishForm>
        {/key}
    {/if}
</Sheet.Root>

<style></style>
