<script lang="ts">
    import Tag from '$lib/components/Tag.svelte';
    import { FicType, type FicCardData } from '$lib/types/domain-models';
    import * as Card from '$ui/card';
    import { TagField, TagInclusion } from '../_types/filter-enums';
    import { getTagsByField } from '../_utils/filter-utils';
    import { pageState } from './state.svelte';

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
                        <Tag
                            label={tag}
                            withCross={false}
                            onclick={() => pageState.withTagFilter(tagField, TagInclusion.Include, tag)}
                        ></Tag>
                    {/each}
                </div>
            {/if}
        {/each}
    </Card.Content>
    <Card.Footer class="flex justify-between">
        <div class="flex gap-2">
            <div>{cardData.fic.rating[0]}</div>
            {#if !cardData.fic.complete}
                <div>❌</div>
            {/if}
            {#if cardData.details.spicy}
                <div>🔥</div>
            {/if}
        </div>

        <div>Words: {cardData.fic.words.toLocaleString('en-us')}</div>
    </Card.Footer>
</Card.Root>

<style></style>
