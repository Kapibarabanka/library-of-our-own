<script lang="ts">
    import type { FicCardData } from '$lib/types/domain-models';
    import { TagField } from '$lib/types/filter-enums';
    import { getTagsByField } from '$lib/utils/filter-utils';
    import Tag from '$lib/components/Tag.svelte';

    const { cardData }: { cardData: FicCardData } = $props();
    const tagTypes = [TagField.Warning, TagField.Fandom, TagField.Ship, TagField.Character, TagField.Tag];
    const authors = cardData.fic.authors ?? ['Anonymous'];
    const rating = cardData.fic.rating[0];
    const complete = cardData.fic.complete ? '✅' : '❌';
</script>

<div class="container">
    <div class="header">
        <div>{rating}</div>
        <div>{complete}</div>
        {#if cardData.details.spicy}
            <div>🔥</div>
        {/if}
        <div class="title">
            <div>
                <a href={cardData.fic.link}>{cardData.fic.title}</a> by{' '}
                {#each authors as author}
                    <!-- TODO add on click -->
                    <Tag label={author}></Tag>
                {/each}
            </div>
        </div>
    </div>
    <div>
        {#each tagTypes as tagType}
            {@const tags = getTagsByField(cardData.fic, tagType)}
            {#if tags.length}
                <div>
                    <span>
                        <strong>{tagType + 's: '}</strong>
                    </span>
                    {#each tags as tag}
                        <!-- TODO add on click -->
                        <Tag label={tag} withCross={false}></Tag>
                    {/each}
                </div>
            {/if}
        {/each}
        <span><strong>Words:</strong> {cardData.fic.words}</span>
    </div>
</div>

<style>
    .container {
        padding: 16px;
        border: 1px solid grey;
        border-radius: 5px;
    }
    .header {
        display: flex;
        justify-content: space-between;
        gap: 8px;
        /* .icons {
        flex: 0 0 40px;
    } */
        .title {
            flex-grow: 1;
        }
    }
</style>
