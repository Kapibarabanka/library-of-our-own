<script lang="ts">
    import type { FinishInfo, HomePageData } from '$lib/types/api-models';
    import { pageState } from './state.svelte';
    import { Label } from '$ui/label';
    import FicCard from '@app/library/FicCard.svelte';
    import { UserImpression, type UserFicKey } from '$lib/types/domain-models';
    import StartedFicCard from './StartedFicCard.svelte';
    import * as Sheet from '$lib/components/ui/sheet';
    import { Button } from '$ui/button';
    import { Checkbox } from '$ui/checkbox';
    import * as ToggleGroup from '$lib/components/ui/toggle-group';
    import Textarea from '$ui/textarea/textarea.svelte';
    import { shortImpression } from '$lib/utils/label-utils';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import LoaderCircle from 'lucide-svelte/icons/loader-circle';
    import FicDetailsClient from '$api/FicDetailsClient';
    import FicsClient from '$api/FicsClient';

    let { homePage }: { homePage: HomePageData } = $props();
    pageState.startedFics = homePage.currentlyReading;

    let isLoading = $state(false);
    let open = $state(false);

    let selectedKey: UserFicKey;

    let abandoned = $state(false);
    let impression = $state<UserImpression | undefined>(undefined);
    let note = $state<string | null>(null);

    function onFinishedPressed(key: UserFicKey) {
        selectedKey = key;
        open = true;
    }

    async function submit() {
        isLoading = true;
        const finishInfo: FinishInfo = {
            key: selectedKey,
            abandoned,
            impression: !impression ? null : impression,
            note,
        };
        await FicDetailsClient.finishFic(finishInfo);
        const newHome = await FicsClient.getHomePage();
        pageState.startedFics = newHome.currentlyReading;
        open = false;
        isLoading = false;
    }

    function onOpenChange(isOpen: boolean) {
        if (!isOpen) {
            abandoned = false;
            impression = undefined;
            note = null;
        }
    }
</script>

<div class="flex flex-col gap-3 p-2">
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground">Currently Reading</Label>
        <div class="flex flex-col gap-2">
            {#each pageState.startedFics as fic}
                <StartedFicCard {fic} onFinish={key => onFinishedPressed(key)}></StartedFicCard>
            {/each}
        </div>
    </div>
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground">Something you wanted to read</Label>
        {#if homePage.randomFicFromBacklog}
            <FicCard cardData={homePage.randomFicFromBacklog}></FicCard>
        {:else}
            <p>No fics in the reading list yet</p>
        {/if}
    </div>
</div>
<Sheet.Root bind:open {onOpenChange}>
    <Sheet.Content side="top">
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
                <ToggleGroup.Root type="single" variant="outline" size="sm" bind:value={impression}>
                    {#each Object.values(UserImpression) as impr}
                        <ToggleGroup.Item value={impr} class="flex gap-1">
                            {getImpressionIcon(impr)}
                            <span class="text-[12px]">{shortImpression(impr)}</span>
                        </ToggleGroup.Item>
                    {/each}
                </ToggleGroup.Root>
                <Textarea bind:value={note} class="text-sm" id="note" placeholder="Add a note if you want to"
                ></Textarea>
            </div>
            <Sheet.Footer>
                <Button onclick={async () => await submit()}>Mark as finished</Button>
            </Sheet.Footer>
        {/if}
    </Sheet.Content>
</Sheet.Root>

<style></style>
