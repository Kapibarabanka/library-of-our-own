<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';
    import { getUserFicKey } from '$lib/utils/fic-utils';
    import { getFic } from '$api/fics.remote';
    import * as Card from '$ui/card';
    import * as Tabs from '$ui/tabs';
    import FicActions from './FicActions.svelte';
    import FicNotes from './FicNotes.svelte';
    import FicInfo from './FicInfo.svelte';
    import FicReadingHistory from './FicReadingHistory.svelte';
    import FicCardFooter from '@app/library/FicCardFooter.svelte';

    let { fic }: { fic: Fic } = $props();
    let userFicKey = $derived(getUserFicKey(fic));
    let stringKey = $derived(`${userFicKey.userId}-${userFicKey.ficType}-${userFicKey.ficId}`);

    async function updateFic() {
        fic = await getFic(userFicKey);
    }
</script>

<div class="flex flex-col gap-3 md:max-w-[700px]">
    <Card.Root class="flex-1 md:min-w-[400px]">
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
        <Card.Footer>
            <FicCardFooter ao3Info={fic.ao3Info} details={fic.details} canFilter={true} />
        </Card.Footer>
    </Card.Root>
    {#key stringKey}
        <Tabs.Root value="actions" class="flex flex-col items-center">
            <Tabs.List class="h-fit w-fit">
                <Tabs.Trigger value="actions">Actions</Tabs.Trigger>
                <Tabs.Trigger class="whitespace-normal" value="history">Reading History</Tabs.Trigger>
                <Tabs.Trigger value="notes">Notes</Tabs.Trigger>
                <Tabs.Trigger class="whitespace-normal" value="info">All Tags</Tabs.Trigger>
            </Tabs.List>
            <div class="w-full mt-1">
                <Tabs.Content value="actions">
                    <FicActions {fic} updateFic={async () => await updateFic()}></FicActions>
                </Tabs.Content>
                <Tabs.Content value="history">
                    <FicReadingHistory bind:fic />
                </Tabs.Content>
                <Tabs.Content value="notes">
                    <FicNotes bind:fic />
                </Tabs.Content>
                <Tabs.Content value="info">
                    <FicInfo bind:fic />
                </Tabs.Content>
            </div>
        </Tabs.Root>
    {/key}
</div>

<style></style>
