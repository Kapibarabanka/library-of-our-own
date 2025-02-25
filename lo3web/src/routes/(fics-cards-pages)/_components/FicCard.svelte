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

    const { cardData }: { cardData: FicCardData } = $props();
    const tagTypes = [TagField.Warning, TagField.Fandom, TagField.Ship, TagField.Character, TagField.Tag];
    const authors = cardData.fic.authors ?? ['Anonymous'];
</script>

<Card.Root>
    <Card.Header>
        <Card.Title class="text-base">
            <a href={cardData.fic.link}>{cardData.fic.title}</a>
        </Card.Title>
        <Card.Description
            >{cardData.key.ficType.toLowerCase()} by {#each authors as author}
                <Tag
                    label={author}
                    onclick={() => pageState.withTagFilter(TagField.Author, TagInclusion.Include, author)}
                ></Tag>
            {/each}
        </Card.Description>
    </Card.Header>
    <Card.Content>
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
            {#if cardData.details.quality}
                <ImpressionBadge
                    impression={cardData.details.quality}
                    onclick={() => pageState.appliedFilters.allowedImpressions.add(cardData.details.quality!)}
                ></ImpressionBadge>
            {/if}
            {#if cardData.details.spicy}
                <div>🔥</div>
            {/if}
        </div>

        <div>Words: {cardData.fic.words.toLocaleString('en-us')}</div>
    </Card.Footer>
</Card.Root>

<style></style>
