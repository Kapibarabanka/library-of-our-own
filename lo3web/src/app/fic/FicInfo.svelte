<script lang="ts">
    import Tag from '$lib/components/Tag.svelte';
    import { type Fic } from '$lib/types/domain-models';
    import * as Card from '$ui/card';

    let { fic = $bindable() }: { fic: Fic } = $props();

    let warnings = $derived(fic.ao3Info.warnings?.at(0) ? fic.ao3Info.warnings : ['No Archive Warnings Apply']);
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

<Card.Root>
    <Card.Content>
        {@render infoBlock('Warnings', warnings)}
        {@render infoBlock('Categories', fic.ao3Info.categories)}
        {@render infoBlock('Fandoms', fic.ao3Info.fandoms)}
        {@render infoBlock('Relationships', fic.ao3Info.relationships)}
        {@render infoBlock('Characters', fic.ao3Info.characters)}
        {@render infoBlock(
            'Additionsl Tags',
            fic.ao3Info.freeformTags?.map(ft => ft.nameInWork),
        )}</Card.Content
    >
</Card.Root>

<style></style>
