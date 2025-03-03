<script lang="ts">
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import Tag from '$lib/components/Tag.svelte';
    import { type FicCardData } from '$lib/types/domain-models';
    import Badge from '$ui/badge/badge.svelte';
    import * as Card from '$ui/card';
    import { TagField, TagInclusion } from '../_types/filter-enums';
    import { getTagsByField } from '../_utils/filter-utils';
    import { pageState } from './state.svelte';
    import RefreshCw from 'lucide-svelte/icons/refresh-cw';
    import Maximize from 'lucide-svelte/icons/maximize-2';
    import Button from '$ui/button/button.svelte';
    import { goto } from '$app/navigation';

    let { cardData }: { cardData: FicCardData } = $props();
    const tagTypes = [TagField.Warning, TagField.Fandom, TagField.Ship, TagField.Character, TagField.Tag];
</script>

<Card.Root>
    <Card.Header class="pr-3">
        <Card.Title class="text-base">
            <div class="flex">
                <a href={cardData.fic.link} class="flex-1">{cardData.fic.title}</a>
                <Button
                    variant="ghost"
                    class="p-1 h-4"
                    onclick={() => goto(`/fic/${cardData.key.ficType.toLowerCase()}-${cardData.key.ficId}`)}
                >
                    <Maximize class="text-muted-foreground" size={15}></Maximize>
                </Button>
            </div>
        </Card.Title>
        <Card.Description
            >{cardData.key.ficType.toLowerCase()} by {#each cardData.fic.authors ?? ['Anonymous'] as author}
                <Tag
                    label={author}
                    onclick={() => pageState.withTagFilter(TagField.Author, TagInclusion.Include, author)}
                ></Tag>
            {/each}
        </Card.Description>
    </Card.Header>
    <Card.Content class="py-2">
        {#each tagTypes as tagField}
            {@const tags = getTagsByField(cardData.fic, tagField)}
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
                rating={cardData.fic.rating}
                onclick={() => pageState.appliedFilters.allowedRatings.add(cardData.fic.rating)}
            ></RatingIcon>
            {#if !cardData.fic.complete}
                <Badge class="px-1.5" variant="outline"><RefreshCw size={15}></RefreshCw></Badge>
            {/if}
            {#if cardData.details.impression}
                <ImpressionBadge
                    impression={cardData.details.impression}
                    onclick={() => pageState.appliedFilters.allowedImpressions.add(cardData.details.impression!)}
                ></ImpressionBadge>
            {/if}
            {#if cardData.details.spicy}
                <Badge class="px-1 text-[15px] leading-4" variant="outline">🔥</Badge>
            {/if}
        </div>

        <div>Words: {cardData.fic.words.toLocaleString('en-us')}</div>
    </Card.Footer>
</Card.Root>

<style></style>
