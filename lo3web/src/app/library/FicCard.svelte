<script lang="ts">
    import Tag from '$lib/components/Tag.svelte';
    import { type FicCardData, type FicDetails } from '$lib/types/domain-models';
    import * as Card from '$ui/card';
    import { TagField, tagFieldLabels, TagInclusion } from './_types/filter-enums';
    import { filterState } from './state.svelte';
    import Ellipsis from 'lucide-svelte/icons/ellipsis-vertical';
    import Button from '$ui/button/button.svelte';
    import { goto } from '$app/navigation';
    import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
    import { toast } from 'svelte-sonner';
    import { patchDetails, startedToday } from '$api/fics-details.remote';
    import KindleDialog from '$lib/components/KindleDialog.svelte';
    import { getContext } from 'svelte';
    import type { User } from '$lib/types/ui-models';
    import FicCardFooter from './FicCardFooter.svelte';

    const tagsToShow = [
        TagField.warnings,
        TagField.fandoms,
        TagField.relationships,
        TagField.characters,
        TagField.freeformTags,
    ];
    const tagsWithLabels = tagsToShow.map(field => ({ field, label: tagFieldLabels[field] }));

    let { cardData, onPatchedDetails }: { cardData: FicCardData; onPatchedDetails: (details: FicDetails) => void } =
        $props();

    let emailSet = !!getContext<User>('user').kindleEmail;
    let kindleDialogOpen = $state(false);

    async function startReading() {
        await startedToday(cardData.key);
        toast('Marked as "In progress"');
    }

    async function toggleBacklog() {
        await patchDetails({
            key: cardData.key,
            details: { ...cardData.details, backlog: !cardData.details.backlog },
        });
        toast(cardData.details.backlog ? 'Removed from backlog' : 'Added to backlog');
        onPatchedDetails({ ...cardData.details, backlog: !cardData.details.backlog });
    }
</script>

<Card.Root>
    <Card.Header>
        <Card.Title class="text-base">
            <div class="flex items-center">
                <a href={`/fic/${cardData.key.ficType.toLowerCase()}-${cardData.key.ficId}`} class="flex-1"
                    >{cardData.ao3Info.title}</a
                >
                <DropdownMenu.Root>
                    <DropdownMenu.Trigger>
                        {#snippet child({ props })}
                            <Button variant="ghost" size="icon-lg" {...props}>
                                <Ellipsis class="text-muted-foreground" size={15}></Ellipsis>
                            </Button>
                        {/snippet}
                    </DropdownMenu.Trigger>
                    <DropdownMenu.Content align="end">
                        <DropdownMenu.Group>
                            <DropdownMenu.Item
                                onclick={() => goto(`/fic/${cardData.key.ficType.toLowerCase()}-${cardData.key.ficId}`)}
                            >
                                See details
                            </DropdownMenu.Item>
                            <DropdownMenu.Item onclick={() => window.open(cardData.ao3Info.link, '_blank')}>
                                Open on AO3
                            </DropdownMenu.Item>
                            <DropdownMenu.Item onclick={() => startReading()}>Start reading</DropdownMenu.Item>
                            {#if !cardData.details.isOnKindle}{/if}
                            <DropdownMenu.Item disabled={!emailSet} onclick={() => (kindleDialogOpen = true)}>
                                {cardData.details.isOnKindle ? 'Mark as "Not on Kindle"' : 'Send to Kindle'}
                            </DropdownMenu.Item>
                            <DropdownMenu.Item onclick={() => toggleBacklog()}>
                                {cardData.details.backlog ? 'Remove from backlog' : 'Add to backlog'}
                            </DropdownMenu.Item>
                        </DropdownMenu.Group>
                    </DropdownMenu.Content>
                </DropdownMenu.Root>
            </div>
        </Card.Title>
        <Card.Description
            >{cardData.key.ficType.toLowerCase()} by {#each cardData.ao3Info.authors ?? ['Anonymous'] as author}
                <Tag
                    label={author}
                    onclick={() => filterState.withTagFilter(TagField.authors, TagInclusion.Include, author)}
                ></Tag>
            {/each}
        </Card.Description>
    </Card.Header>
    <Card.Content class="py-2">
        {#each tagsWithLabels as { field, label }}
            {#if field === TagField.freeformTags}
                {@const tags = cardData.ao3Info.freeformTags ?? []}
                {#if tags.length}
                    <div>
                        <span class="font-semibold text-sm">{label + 's: '}</span>
                        {#each tags as tag}
                            <Tag
                                label={tag.nameInWork}
                                onclick={() =>
                                    filterState.withTagFilter(field, TagInclusion.Include, tag.canonicalName)}
                            ></Tag>
                        {/each}
                    </div>
                {/if}
            {:else}
                {@const tags = cardData.ao3Info[field] ?? []}
                {#if tags.length}
                    <div>
                        <span class="font-semibold text-sm">{label + 's: '}</span>
                        {#each tags as tag}
                            <Tag label={tag} onclick={() => filterState.withTagFilter(field, TagInclusion.Include, tag)}
                            ></Tag>
                        {/each}
                    </div>
                {/if}
            {/if}
        {/each}
    </Card.Content>
    <Card.Footer>
        <FicCardFooter ao3Info={cardData.ao3Info} details={cardData.details} canFilter={true} />
    </Card.Footer>
</Card.Root>
<KindleDialog
    bind:open={kindleDialogOpen}
    ficName={cardData.ao3Info.title}
    ficKey={cardData.key}
    details={cardData.details}
    onSubmitted={async () => onPatchedDetails({ ...cardData.details, isOnKindle: !cardData.details.isOnKindle })}
></KindleDialog>

<style></style>
