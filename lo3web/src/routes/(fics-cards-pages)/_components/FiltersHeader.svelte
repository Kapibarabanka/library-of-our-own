<script lang="ts">
    import {
        BoolField,
        filterableFields,
        FilterType,
        TagField,
        TagInclusion,
        type FilterableField,
    } from '../_types/filter-enums';
    import { getFilterType } from '../_utils/filter-utils';
    import { pageState } from './state.svelte';
    import TagFilter from './Filters/TagFilter.svelte';
    import BoolFilter from './Filters/BoolFilter.svelte';
    import * as Select from '$ui/select';
    import Label from '$ui/label/label.svelte';
    import BadgeTag from '$lib/components/BadgeTag.svelte';

    let filteredField: FilterableField = $state(TagField.Ship);
    let filterType = $derived(getFilterType(filteredField));
</script>

<div class="flex gap-2 items-center pl-3">
    <Label for="filter-type">Filter by</Label>
    <div id="filter-type" class="flex-1">
        <Select.Root type="single" name="favoriteFruit" bind:value={filteredField}>
            <Select.Trigger class="w-full">
                {filteredField}
            </Select.Trigger>
            <Select.Content>
                <Select.Group>
                    {#each filterableFields as filterableField}
                        <Select.Item value={filterableField} label={filterableField}></Select.Item>
                    {/each}
                </Select.Group>
            </Select.Content>
        </Select.Root>
    </div>
</div>

{#if filterType === FilterType.Tag}
    <TagFilter filteredField={filteredField as TagField}></TagFilter>
{:else if filterType === FilterType.Bool}
    <BoolFilter filteredField={filteredField as BoolField}></BoolFilter>
{/if}
<div>
    {#if pageState.hasApplied}
        <Label>Applied filters:</Label>
        {#each pageState.appliedFilters.includedTagFilters as [tagType, includedTags]}
            {#each includedTags as tag}
                <BadgeTag
                    label={tag}
                    striked={false}
                    onclick={() => pageState.withoutTagFilter(tagType, TagInclusion.Include, tag)}
                ></BadgeTag>
            {/each}
        {/each}
        {#each pageState.appliedFilters.excludedTagFilters as [tagType, excludedTags]}
            {#each excludedTags as tag}
                <BadgeTag
                    label={tag}
                    striked={true}
                    onclick={() => pageState.withoutTagFilter(tagType, TagInclusion.Exclude, tag)}
                ></BadgeTag>
            {/each}
        {/each}
        {#each pageState.appliedFilters.boolFilters as [field, value]}
            <BadgeTag label={field} striked={!value} onclick={() => pageState.appliedFilters.boolFilters.delete(field)}
            ></BadgeTag>
        {/each}
    {/if}
</div>

<style></style>
