<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
    import { formatDate } from '$lib/utils/label-utils';
    import * as Item from '$ui/item';
    import { Button } from '$ui/button';
    import EditHistory from './EditHistory.svelte';
    import { getUserFicKey } from '$lib/utils/fic-utils';

    let { fic = $bindable(), updateFic }: { fic: Fic; updateFic: () => Promise<void> } = $props();
    let key = $derived(getUserFicKey(fic));

    let formattedDates = $derived(
        fic.readDatesInfo?.readDates?.map(rd =>
            rd.finishDate
                ? {
                      date: `${formatDate(rd.startDate)} — ${formatDate(rd.finishDate)}`,
                      status: rd.isAbandoned ? 'Dropped' : 'Finished',
                  }
                : { date: `Started on ${formatDate(rd.startDate)}`, status: '' },
        ) ?? [],
    );

    let editMode = $state(false);

    function startEditing() {
        editMode = true;
    }

    function onCancel() {
        editMode = false;
    }

    async function onEditFinished() {
        editMode = false;
        await updateFic();
    }
</script>

<div class="flex flex-col gap-2">
    {#if editMode}
        <EditHistory
            {key}
            originalDates={fic.readDatesInfo?.readDates ?? []}
            editFinished={onEditFinished}
            cancel={onCancel}
        />
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
