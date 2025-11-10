<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
    import * as Accordion from '$lib/components/ui/accordion';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import Button from '$ui/button/button.svelte';
    import * as Item from '$lib/components/ui/item/index.js';
    import * as Sheet from '$lib/components/ui/sheet';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { startedToday } from '$api/fics-details.remote';
    import { toast } from 'svelte-sonner';
    import { getUserFicKey } from '$lib/utils/fic-utils';
    import { getFic } from '$api/fics.remote';
    import FinishForm from '$lib/components/FinishForm.svelte';
    import * as Card from '$ui/card';
    import moment from 'moment';

    let { fic }: { fic: Fic } = $props();
    let impression = $derived(
        [
            ...(fic.details.impression
                ? [getImpressionIcon(fic.details.impression) + ' ' + fic.details.impression]
                : []),
            ...(fic.details.spicy ? ['🔥 Spicy'] : []),
        ].join(' and ')
    );

    let key = getUserFicKey(fic);

    async function startReading() {
        await startedToday(key);
        toast('Marked as "In progress"');
        fic = await getFic(key);
    }

    let sheetOpened = $state(false);

    function onFinishPressed() {
        sheetOpened = true;
    }

    async function onSubmitted() {
        fic = await getFic(key);
        sheetOpened = false;
    }
</script>

<div class="flex flex-col p-4">
    <a href={fic.ao3Info.link} target="_blank" class="flex-1 text-lg font-semibold">{fic.ao3Info.title}</a>
    <div class="flex justify-between gap-6 text-sm text-secondary-foreground">
        <span>by {fic.ao3Info.authors?.join(', ')}</span>
        <span
            ><RatingIcon rating={fic.ao3Info.rating} variant="letter"></RatingIcon>
            {fic.ao3Info.words.toLocaleString('en-us')} words</span
        >
    </div>

    <span class="text-sm mt-2">{fic.ao3Info.relationships?.join(', ')} </span>
    <div class="flex flex-col gap-1 mt-2">
        {#if fic.readDatesInfo.canStart}
            <Button onclick={() => startReading()}>Start reading</Button>
        {:else if fic.readDatesInfo.canFinish}
            <Item.Root variant="default" size="sm" class="px-0">
                <Item.Content>
                    <Item.Title>
                        Started reading on {fic.readDatesInfo.readDates[fic.readDatesInfo.readDates.length - 1]
                            .startDate}
                    </Item.Title>
                </Item.Content>
                <Item.Actions>
                    <Button size="sm" onclick={() => onFinishPressed()}>Finish</Button>
                </Item.Actions>
            </Item.Root>
        {/if}
        <Item.Root variant="default" size="sm" class="px-0">
            <Item.Content>
                <Item.Title>
                    {#if impression}
                        <span>{impression}</span>
                    {:else}
                        <span>Not rated yet</span>{/if}
                </Item.Title>
                {#if impression}
                    <Item.Description class="text-xs">Your impression</Item.Description>
                {/if}
            </Item.Content>
            <Item.Actions>
                <Button variant="outline" size="sm">Rate</Button>
            </Item.Actions>
        </Item.Root>
        <Item.Root variant="default" size="sm" class="px-0">
            <Item.Content>
                <Item.Title>{fic.details.backlog ? 'In backlog' : 'Not in backlog'}</Item.Title>
            </Item.Content>
            <Item.Actions>
                <Button variant="outline" size="sm">
                    {fic.details.backlog ? 'Remove from backlog' : 'Add to backlog'}
                </Button>
            </Item.Actions>
        </Item.Root>
        <Item.Root variant="default" size="sm" class="px-0">
            <Item.Content>
                <Item.Title>{fic.details.isOnKindle ? 'On Kindle' : 'Not on Kindle'}</Item.Title>
            </Item.Content>
            <Item.Actions>
                <Button variant="outline" size="sm"
                    >{fic.details.isOnKindle ? 'Mark as "Not on Kindle"' : 'Send to Kindle'}
                </Button>
            </Item.Actions>
        </Item.Root>
    </div>
    <Accordion.Root type="multiple" value={[]}>
        <Accordion.Item value="history">
            <Accordion.Trigger>Your reading history</Accordion.Trigger>
            <Accordion.Content>
                <div class="flex flex-col gap-1">
                    {#each fic.readDatesInfo.readDates ?? [] as readDates}
                        <Item.Root variant="outline" size="sm">
                            <Item.Content>
                                <div class="flex gap-2 justify-between">
                                    <span>From {readDates.startDate} to {readDates.finishDate}</span>
                                    <span>
                                        {readDates.isAbandoned
                                            ? 'Abandoned'
                                            : readDates.finishDate
                                              ? 'Finished'
                                              : 'In progress'}
                                    </span>
                                </div>
                            </Item.Content>
                        </Item.Root>
                    {/each}
                </div>
            </Accordion.Content>
        </Accordion.Item>
        <Accordion.Item value="notes">
            <Accordion.Trigger>Your notes</Accordion.Trigger>
            <Accordion.Content>
                <!-- <Button>Add Note</Button> -->
                <div class="flex flex-col gap-2">
                    {#each fic.notes ?? [] as note}
                        <Card.Root>
                            <Card.Header>
                                <Card.Title>{moment(note.date).format('MMM Do, YYYY')}</Card.Title>
                            </Card.Header>
                            <Card.Content>{note.text}</Card.Content>
                        </Card.Root>
                    {/each}
                </div>
            </Accordion.Content>
        </Accordion.Item>
        <Accordion.Item value="info">
            <Accordion.Trigger>Information from AO3</Accordion.Trigger>
            <Accordion.Content>
                <!-- <Button>Update</Button>
                <div>{JSON.stringify(fic.ao3Info)}</div> -->
            </Accordion.Content>
        </Accordion.Item>
    </Accordion.Root>
</div>

<Sheet.Root bind:open={sheetOpened}>
    <FinishForm {key} details={fic.details} {onSubmitted}></FinishForm>
</Sheet.Root>

<style></style>
