<script lang="ts">
    import { updateAo3Info } from '$api/fics.remote';
    import Tag from '$lib/components/Tag.svelte';
    import { FicType, type Fic } from '$lib/types/domain-models';
    import { getFicKey } from '$lib/utils/fic-utils';
    import { Button } from '$ui/button';
    import * as Card from '$ui/card';
    import * as Item from '$ui/item';
    import { Spinner } from '$ui/spinner';

    let { fic = $bindable() }: { fic: Fic } = $props();
    let partType = $derived(fic.ao3Info.ficType === FicType.Work ? 'chapter' : 'work');
    let multiple = $derived(fic.ao3Info.partsWritten === 1 ? '' : 's');
    let completed = $derived(fic.ao3Info.complete ? 'Completed' : 'Not completed');
    let completionInfo = $derived(`${completed}, ${fic.ao3Info.partsWritten} ${partType}${multiple}`);

    // #each is not updated if we don't forcibly update the state of parent object
    // derived info didn't work either
    let info = $state(fic.ao3Info);
    let warnings = $derived(info.warnings?.at(0) ? info.warnings : ['No Archive Warnings Apply']);

    let loading = $state(false);

    async function updateInfo() {
        loading = true;
        const newInfo = await updateAo3Info(getFicKey(fic));
        fic.ao3Info = newInfo;
        info = newInfo;
        loading = false;
    }
</script>

{#snippet infoBlock(title: string, values: string[] | undefined)}
    <div>
        <span class="font-semibold text-sm">{title}:</span>
        {#if values}
            {#each values as val}
                <Tag label={val} />
            {/each}
        {/if}
    </div>
{/snippet}

<div class="flex flex-col gap-2">
    <Item.Root variant="outline" size="sm">
        <Item.Content>
            <Item.Title>{completionInfo}</Item.Title>
            <Item.Description class="text-xs">
                Click to update fic in our database if new {partType}s were posted
            </Item.Description>
        </Item.Content>
        <Item.Actions>
            <Button disabled={loading} onclick={() => updateInfo()} size="sm">
                {#if loading}
                    <Spinner />
                {/if}
                Update
            </Button>
        </Item.Actions>
    </Item.Root>

    <Card.Root>
        <Card.Content>
            {@render infoBlock('Warnings', warnings)}
            {@render infoBlock('Categories', info.categories)}
            {@render infoBlock('Fandoms', info.fandoms)}
            {@render infoBlock('Relationships', info.relationships)}
            {@render infoBlock('Characters', info.characters)}
            {@render infoBlock('Additionsl Tags', info.tags)}</Card.Content
        >
    </Card.Root>
</div>

<style></style>
