<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
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
    import * as Tabs from '$ui/tabs';
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

<div class="flex flex-col p-4 gap-3">
    <Card.Root>
        <Card.Header>
            <Card.Title class="text-lg">
                <a href={fic.ao3Info.link} target="_blank">{fic.ao3Info.title}</a>
            </Card.Title>
            <Card.Description>
                {fic.ao3Info.ficType.toLowerCase()} by {fic.ao3Info.authors?.join(', ')}
            </Card.Description>
        </Card.Header>
        <Card.Content class="py-3 leading-snug text-sm">
            <span>{fic.ao3Info.relationships?.join(', ')} </span>
        </Card.Content>
        <Card.Footer class="gap-2 justify-between">
            <RatingIcon rating={fic.ao3Info.rating} variant="full"></RatingIcon>
            <span>{fic.ao3Info.words.toLocaleString('en-us')} words </span>
        </Card.Footer>
    </Card.Root>

    <Tabs.Root value="actions">
        <Tabs.List class="h-fit">
            <Tabs.Trigger value="actions">Actions</Tabs.Trigger>
            <Tabs.Trigger class="whitespace-normal" value="history">Reading history</Tabs.Trigger>
            <Tabs.Trigger value="notes">Notes</Tabs.Trigger>
            <Tabs.Trigger class="whitespace-normal" value="info">Information from Ao3</Tabs.Trigger>
        </Tabs.List>
        <Tabs.Content value="actions" class="bg-muted/50">
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
                                Started reading on {fic.readDatesInfo.readDates[fic.readDatesInfo.readDates.length - 1]
                                    .startDate}
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
                <Item.Root variant="default" size="sm">
                    <Item.Content>
                        <Item.Title>{fic.details.backlog ? 'In reading list' : 'Not in reading list'}</Item.Title>
                    </Item.Content>
                    <Item.Actions>
                        <Button variant="outline" size="sm">
                            {fic.details.backlog ? 'Remove from reading list' : 'Add to reading list'}
                        </Button>
                    </Item.Actions>
                </Item.Root>
                <Item.Root variant="default" size="sm">
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
        </Tabs.Content>
        <Tabs.Content value="history">
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
        </Tabs.Content>
        <Tabs.Content value="notes">
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
        </Tabs.Content>
        <Tabs.Content value="info">
            <!-- <Button>Update</Button>
                <div>{JSON.stringify(fic.ao3Info)}</div> -->
        </Tabs.Content>
    </Tabs.Root>
</div>

<Sheet.Root bind:open={sheetOpened}>
    <FinishForm {key} details={fic.details} {onSubmitted}></FinishForm>
</Sheet.Root>

<style></style>
