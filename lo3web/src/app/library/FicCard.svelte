<script lang="ts">
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import Tag from '$lib/components/Tag.svelte';
    import { type FicCardData, type FicDetails } from '$lib/types/domain-models';
    import Badge from '$ui/badge/badge.svelte';
    import * as Card from '$ui/card';
    import { BoolField, TagField, TagInclusion } from './_types/filter-enums';
    import { getTagsByField } from './_utils/filter-utils';
    import { pageState } from './state.svelte';
    import RefreshCw from 'lucide-svelte/icons/refresh-cw';
    import Ellipsis from 'lucide-svelte/icons/ellipsis-vertical';
    import Tablet from 'lucide-svelte/icons/tablet';
    import Button from '$ui/button/button.svelte';
    import { goto } from '$app/navigation';
    import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
    import { toast } from 'svelte-sonner';
    import { patchDetails, startedToday } from '$api/fics-details.remote';
    import IconBadge from '$lib/components/IconBadge.svelte';
    import KindleDialog from '$lib/components/KindleDialog.svelte';

    let { cardData, onPatchedDetails }: { cardData: FicCardData; onPatchedDetails: (details: FicDetails) => void } =
        $props();
    const tagTypes = [TagField.Warning, TagField.Fandom, TagField.Ship, TagField.Character, TagField.Tag];

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
        toast(cardData.details.backlog ? 'Removed from reading list' : 'Added to reading list');
        onPatchedDetails({ ...cardData.details, backlog: !cardData.details.backlog });
    }
</script>

<Card.Root>
    <Card.Header>
        <Card.Title class="text-base">
            <div class="flex">
                <a href={`/fic/${cardData.key.ficType.toLowerCase()}-${cardData.key.ficId}`} class="flex-1"
                    >{cardData.ao3Info.title}</a
                >
                <DropdownMenu.Root>
                    <DropdownMenu.Trigger>
                        {#snippet child({ props })}
                            <Button variant="ghost" class="p-1 h-4" {...props}>
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
                            <DropdownMenu.Item onclick={() => (kindleDialogOpen = true)}>
                                {cardData.details.isOnKindle ? 'Mark as "Not on Kindle"' : 'Send to Kindle'}
                            </DropdownMenu.Item>
                            <DropdownMenu.Item onclick={() => toggleBacklog()}>
                                {cardData.details.backlog ? 'Remove from reading list' : 'Add to reading list'}
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
                    onclick={() => pageState.withTagFilter(TagField.Author, TagInclusion.Include, author)}
                ></Tag>
            {/each}
        </Card.Description>
    </Card.Header>
    <Card.Content class="py-2">
        {#each tagTypes as tagField}
            {@const tags = getTagsByField(cardData.ao3Info, tagField)}
            {#if tags.length}
                <div>
                    <span class="font-semibold">{tagField + 's: '}</span>
                    {#each tags as tag}
                        <Tag label={tag} onclick={() => pageState.withTagFilter(tagField, TagInclusion.Include, tag)}
                        ></Tag>
                    {/each}
                </div>
            {/if}
        {/each}
    </Card.Content>
    <Card.Footer class="flex justify-between">
        <div class="flex gap-2">
            <RatingIcon
                rating={cardData.ao3Info.rating}
                onclick={() => pageState.appliedFilters.allowedRatings.add(cardData.ao3Info.rating)}
            ></RatingIcon>
            {#if !cardData.ao3Info.complete}
                <Badge class="px-1.5" variant="outline"><RefreshCw size={15}></RefreshCw></Badge>
            {/if}
            {#if cardData.details.isOnKindle}
                <Badge
                    class="px-1.5"
                    variant="outline"
                    onclick={() => pageState.appliedFilters.boolFilters.set(BoolField.OnKindle, true)}
                >
                    <Tablet size={15}></Tablet>
                </Badge>
            {/if}
            {#if cardData.details.impression}
                <ImpressionBadge
                    impression={cardData.details.impression}
                    onclick={() => pageState.appliedFilters.allowedImpressions.add(cardData.details.impression!)}
                ></ImpressionBadge>
            {/if}
            {#if cardData.details.spicy}
                <IconBadge icon="🔥"></IconBadge>
            {/if}
        </div>

        <div>Words: {cardData.ao3Info.words.toLocaleString('en-us')}</div>
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
