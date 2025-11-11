<script lang="ts">
    import type { Fic, UserImpression } from '$lib/types/domain-models';
    import { Button, buttonVariants } from '$ui/button/index.js';
    import * as Item from '$lib/components/ui/item/index.js';
    import * as Sheet from '$lib/components/ui/sheet';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { patchDetails, startedToday } from '$api/fics-details.remote';
    import { toast } from 'svelte-sonner';
    import { getUserFicKey } from '$lib/utils/fic-utils';
    import FinishForm from '$lib/components/FinishForm.svelte';
    import * as Dialog from '$ui/dialog';
    import { Checkbox } from '$ui/checkbox';
    import { Label } from '$ui/label';
    import ImpressionInput from '$lib/components/ImpressionInput.svelte';
    import { formatDate } from '$lib/utils/label-utils';

    let { fic, updateFic }: { fic: Fic; updateFic: () => Promise<void> } = $props();
    let key = getUserFicKey(fic);
    let spicy = $state(fic.details.spicy);
    let impression = $state<UserImpression | ''>(fic.details.impression ?? '');

    let formattedImpression = $derived(
        [
            ...(fic.details.impression
                ? [getImpressionIcon(fic.details.impression) + ' ' + fic.details.impression]
                : []),
            ...(fic.details.spicy ? ['🔥 Spicy'] : []),
        ].join(' and ')
    );

    let sheetOpened = $state(false);
    let rateDialogOpen = $state(false);
    let sendDialogOpen = $state(false);

    async function startReading() {
        await startedToday(key);
        toast('Marked as "In progress"');
        await updateFic();
    }

    function onFinishPressed() {
        sheetOpened = true;
    }

    async function onFinishSubmitted() {
        await updateFic();
        sheetOpened = false;
    }

    async function onRate() {
        await patchDetails({
            key,
            details: { ...fic.details, spicy: spicy, impression: !impression ? undefined : impression },
        });
        await updateFic();
        rateDialogOpen = false;
    }

    async function toggleBacklog() {
        await patchDetails({
            key,
            details: { ...fic.details, backlog: !fic.details.backlog },
        });
        await updateFic();
    }

    async function toggleKindle() {
        await patchDetails({
            key,
            details: { ...fic.details, isOnKindle: !fic.details.isOnKindle },
        });
        await updateFic();
    }
</script>

<div class="flex flex-col gap-1 mt-2">
    {#if fic.readDatesInfo.canStart}
        <Item.Root variant="default" size="sm">
            <Item.Content>
                <Button variant="outline" onclick={() => startReading()}>Start reading</Button>
            </Item.Content>
        </Item.Root>
    {:else if fic.readDatesInfo.canFinish}
        <Item.Root variant="default" size="sm">
            <Item.Content>
                <Item.Title>
                    Started reading on {formatDate(fic.readDatesInfo.readDates[0].startDate)}
                </Item.Title>
            </Item.Content>
            <Item.Actions>
                <Button size="sm" variant="outline" onclick={() => onFinishPressed()}>Finish</Button>
            </Item.Actions>
        </Item.Root>
    {/if}
    <Item.Root variant="default" size="sm">
        <Item.Content>
            <Item.Title>
                {#if formattedImpression}
                    <span>{formattedImpression}</span>
                {:else}
                    <span>Not rated yet</span>{/if}
            </Item.Title>
            {#if formattedImpression}
                <Item.Description class="text-xs">Your impression</Item.Description>
            {/if}
        </Item.Content>
        <Item.Actions>
            <Dialog.Root bind:open={rateDialogOpen}>
                <Dialog.Trigger class={buttonVariants({ variant: 'outline', size: 'sm' })}>Rate</Dialog.Trigger>
                <Dialog.Content class="sm:max-w-[425px]">
                    <Dialog.Header>
                        <Dialog.Title>Rate this fic</Dialog.Title>
                    </Dialog.Header>
                    <div class="flex items-center space-x-2">
                        <Checkbox id="spicy" bind:checked={spicy} />
                        <Label for="spicy">
                            <span class="text-sm font-medium leading-none">🔥 Spicy</span>
                        </Label>
                    </div>
                    <ImpressionInput bind:impression></ImpressionInput>
                    <Dialog.Footer>
                        <Button onclick={() => onRate()}>Save changes</Button>
                    </Dialog.Footer>
                </Dialog.Content>
            </Dialog.Root>
        </Item.Actions>
    </Item.Root>
    <Item.Root variant="default" size="sm">
        <Item.Content>
            <Item.Title>{fic.details.backlog ? 'In reading list' : 'Not in reading list'}</Item.Title>
        </Item.Content>
        <Item.Actions>
            <Button variant="outline" size="sm" onclick={() => toggleBacklog()}>
                {fic.details.backlog ? 'Remove from reading list' : 'Add to reading list'}
            </Button>
        </Item.Actions>
    </Item.Root>
    <Item.Root variant="default" size="sm">
        <Item.Content>
            <Item.Title>{fic.details.isOnKindle ? 'On Kindle' : 'Not on Kindle'}</Item.Title>
        </Item.Content>
        <Item.Actions>
            {#if fic.details.isOnKindle}
                <Button variant="outline" size="sm" onclick={() => toggleKindle()}>Mark as "Not on Kindle"</Button>
            {:else}
                <Button variant="outline" size="sm" onclick={() => toggleKindle()}>Send to Kindle</Button>
            {/if}
        </Item.Actions>
    </Item.Root>
</div>
<Sheet.Root bind:open={sheetOpened}>
    <FinishForm {key} details={fic.details} onSubmitted={onFinishSubmitted}></FinishForm>
</Sheet.Root>

<style></style>
