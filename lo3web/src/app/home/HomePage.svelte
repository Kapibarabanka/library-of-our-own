<script lang="ts">
    import type { FinishInfo, HomePageData } from '$lib/types/api-models';
    import { Label } from '$ui/label';
    import FicCard from '@app/library/FicCard.svelte';
    import { UserImpression, type FicCardData, type UserFicKey } from '$lib/types/domain-models';
    import StartedFicCard from './StartedFicCard.svelte';
    import * as Sheet from '$lib/components/ui/sheet';
    import { Button } from '$ui/button';
    import { Checkbox } from '$ui/checkbox';
    import Textarea from '$ui/textarea/textarea.svelte';
    import { shortImpression } from '$lib/utils/label-utils';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import LoaderCircle from 'lucide-svelte/icons/loader-circle';
    import GeneralStatsChart from './GeneralStatsChart.svelte';
    import { finishFic } from '$api/fics-details.remote';
    import { getHomePage } from '$api/fics.remote';
    import * as Tabs from '$lib/components/ui/tabs';
    import Separator from '$ui/separator/separator.svelte';

    let { homePage }: { homePage: HomePageData } = $props();

    let isLoading = $state(false);
    let open = $state(false);

    let selectedKey: UserFicKey;

    let abandoned = $state(false);
    let spicy = $state(false);
    let impression = $state<UserImpression | undefined>(undefined);
    let note = $state<string | undefined>(undefined);

    function onFinishedPressed(fic: FicCardData) {
        selectedKey = fic.key;
        spicy = fic.details.spicy;
        impression = fic.details.impression;
        open = true;
    }

    async function submit() {
        isLoading = true;
        const finishInfo: FinishInfo = {
            key: selectedKey,
            abandoned,
            spicy,
            impression: !impression ? undefined : impression,
            note,
        };
        try {
            await finishFic(finishInfo).updates(getHomePage());
        } catch (e) {
            alert(JSON.stringify(e));
        }
        open = false;
        isLoading = false;
    }

    function onOpenChange(isOpen: boolean) {
        if (!isOpen) {
            abandoned = false;
            impression = undefined;
            note = undefined;
        }
    }
</script>

<div class="flex flex-col gap-4 p-4">
    <div class="flex flex-col gap-2">
        <h3 class="scroll-m-20 border-b pb-1 text-xl text-muted-foreground font-semibold tracking-tight">
            Currently Reading
        </h3>
        <div class="flex flex-col gap-2">
            {#each homePage.currentlyReading as fic}
                <StartedFicCard {fic} onFinish={fic => onFinishedPressed(fic)}></StartedFicCard>
            {/each}
        </div>
    </div>
    <div class="flex flex-col gap-2">
        <h3 class="scroll-m-20 border-b pb-1 text-xl text-muted-foreground font-semibold tracking-tight">
            Something you wanted to read
        </h3>
        <!-- <Label class="text-center text-sm font-bold text-muted-foreground">Something you wanted to read</Label> -->
        {#if homePage.randomFicFromBacklog}
            <FicCard cardData={homePage.randomFicFromBacklog}></FicCard>
        {:else}
            <p>No fics in the reading list yet</p>
        {/if}
    </div>
    <Separator />
    <GeneralStatsChart stats={homePage.generalStats}></GeneralStatsChart>
</div>
<!-- TODO: extract sheet with finish info into component and use in other places -->
<Sheet.Root bind:open {onOpenChange}>
    <Sheet.Content side="top" class="p-3">
        {#if isLoading}
            <div class="flex h-[280px] items-center text-muted-foreground">
                <LoaderCircle size={80} class="animate-spin flex-1" />
            </div>
        {:else}
            <Sheet.Header>
                <Sheet.Title>How was this fic?</Sheet.Title>
            </Sheet.Header>
            <div class="flex flex-col gap-3 py-4">
                <div class="flex items-center space-x-2">
                    <Checkbox id="abandoned" bind:checked={abandoned} />
                    <Label for="abandoned">
                        <span class="text-sm font-medium leading-none">Abandoned</span>
                        <span class="text-muted-foreground text-xs">(fic won't be included in statistics)</span>
                    </Label>
                </div>
                <div class="flex items-center space-x-2">
                    <Checkbox id="spicy" bind:checked={spicy} />
                    <Label for="spicy">
                        <span class="text-sm font-medium leading-none">🔥 Spicy</span>
                    </Label>
                </div>
                <Tabs.Root bind:value={impression} class="max-w-[500px]">
                    <Tabs.List class="grid w-full grid-cols-5">
                        {#each Object.values(UserImpression) as impr}
                            <Tabs.Trigger value={impr} class="flex gap-1">
                                {getImpressionIcon(impr)}
                                <span class="text-[12px]">{shortImpression(impr)}</span>
                            </Tabs.Trigger>
                        {/each}
                    </Tabs.List>
                </Tabs.Root>
                <Textarea bind:value={note} class="text-sm" id="note" placeholder="Add a note if you want to" />
            </div>
            <Sheet.Footer>
                <Button onclick={async () => await submit()}>Mark as finished</Button>
            </Sheet.Footer>
        {/if}
    </Sheet.Content>
</Sheet.Root>

<style></style>
