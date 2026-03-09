<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
    import { formatDate } from '$lib/utils/label-utils';
    import * as Item from '$ui/item';
    import { Button } from '$ui/button';
    import { Plus } from '@lucide/svelte';

    let { fic = $bindable() }: { fic: Fic } = $props();

    let formattedDates = $derived(
        fic.readDatesInfo?.readDates?.map(rd =>
            rd.finishDate
                ? {
                      date: `${formatDate(rd.startDate)} — ${formatDate(rd.finishDate)}`,
                      status: rd.isAbandoned ? 'Abandoned' : 'Finished',
                  }
                : { date: `Started on ${formatDate(rd.startDate)}`, status: '' },
        ) ?? [],
    );
    let editedDates = $state($state.snapshot(fic.readDatesInfo?.readDates));

    let editMode = $state(false);

    function startEditing() {
        editedDates = $state.snapshot(fic.readDatesInfo?.readDates);

        editMode = true;
    }
</script>

<div class="flex flex-col gap-2">
    {#if editMode}
        <div class="flex justify-between">
            <Button class="md:w-fit" variant="outline" onclick={() => {}}><Plus />Add record</Button>
            <div class="flex gap-4">
                <Button class="md:w-fit" variant="outline" onclick={() => {}}>Cancel</Button>
                <Button class="md:w-fit" onclick={() => {}}>Save</Button>
            </div>
        </div>
        <Item.Group>
            {#each editedDates as readDate, index}
                <Item.Root size="sm">
                    <Item.Content>
                        <Item.Title>{readDate.startDate} - {readDate.finishDate}</Item.Title>
                    </Item.Content>
                    <Item.Actions></Item.Actions>
                </Item.Root>
                {#if index !== editedDates.length - 1}
                    <Item.Separator />
                {/if}
            {/each}
        </Item.Group>
    {:else}
        <Button class="md:w-fit" onclick={() => startEditing()}>Edit history</Button>
        {#if formattedDates?.length}
            {#each formattedDates as readDate}
                <Item.Root variant="outline" size="sm" class="w-full">
                    <Item.Content>
                        <div class="flex gap-2 justify-between">
                            <span>{readDate.date}</span>
                            <span>{readDate.status} </span>
                        </div>
                    </Item.Content>
                </Item.Root>
            {/each}
        {:else}
            <span>You haven't read this fic yet</span>
        {/if}
    {/if}
</div>

<style></style>
