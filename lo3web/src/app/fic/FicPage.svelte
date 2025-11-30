<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import * as Item from '$lib/components/ui/item/index.js';
    import { getUserFicKey } from '$lib/utils/fic-utils';
    import { getFic } from '$api/fics.remote';
    import * as Card from '$ui/card';
    import * as Tabs from '$ui/tabs';
    import { formatDate } from '$lib/utils/label-utils';
    import FicActions from './FicActions.svelte';
    import FicNotes from './FicNotes.svelte';
    import FicInfo from './FicInfo.svelte';

    let { fic }: { fic: Fic } = $props();
    let key = getUserFicKey(fic);

    let formattedDates = $derived(
        fic.readDatesInfo?.readDates?.map(rd =>
            rd.finishDate
                ? {
                      date: `${formatDate(rd.startDate)} - ${formatDate(rd.finishDate)}`,
                      status: rd.isAbandoned ? 'Abandoned' : 'Finished',
                  }
                : { date: `Started on ${formatDate(rd.startDate)}`, status: '' }
        ) ?? []
    );

    async function updateFic() {
        fic = await getFic(key);
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
            <FicActions {fic} updateFic={async () => await updateFic()}></FicActions>
        </Tabs.Content>
        <Tabs.Content value="history">
            <div class="flex flex-col gap-2">
                {#each formattedDates as readDate}
                    <Item.Root variant="outline" size="sm">
                        <Item.Content>
                            <div class="flex gap-2 justify-between">
                                <span>{readDate.date}</span>
                                <span>{readDate.status} </span>
                            </div>
                        </Item.Content>
                    </Item.Root>
                {/each}
            </div>
        </Tabs.Content>
        <Tabs.Content value="notes">
            <FicNotes {fic} />
        </Tabs.Content>
        <Tabs.Content value="info">
            <FicInfo {fic} />
        </Tabs.Content>
    </Tabs.Root>
</div>

<style></style>
