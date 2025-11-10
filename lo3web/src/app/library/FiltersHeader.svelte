<script lang="ts">
    import {
        BoolField,
        CustomField,
        filterableFields,
        FilterType,
        TagField,
        TagInclusion,
        type FilterableField,
    } from './_types/filter-enums';
    import { pageState } from './state.svelte';
    import * as Select from '$ui/select';
    import Label from '$ui/label/label.svelte';
    import BadgeTag from '$lib/components/BadgeTag.svelte';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import { getFilterType } from './_utils/filter-utils';
    import TagFilter from './filters/TagFilter.svelte';
    import BoolFilter from './filters/BoolFilter.svelte';
    import RatingFilter from './filters/RatingFilter.svelte';
    import ImpressionFilter from './filters/ImpressionFilter.svelte';

    let filteredField: FilterableField = $state(TagField.Ship);
    let filterType = $derived(getFilterType(filteredField));
</script>

<div class="flex flex-col gap-2">
    <div id="filter-type">
        <Select.Root type="single" bind:value={filteredField}>
            <Select.Trigger class="w-full">
                {filteredField}
            </Select.Trigger>
            <Select.Content preventScroll={true}>
                <Select.Group>
                    {#each filterableFields as filterableField}
                        <Select.Item value={filterableField} label={filterableField}></Select.Item>
                    {/each}
                </Select.Group>
            </Select.Content>
        </Select.Root>
    </div>

    {#if filterType === FilterType.Tag}
        <TagFilter filteredField={filteredField as TagField}></TagFilter>
    {:else if filterType === FilterType.Bool}
        <BoolFilter filteredField={filteredField as BoolField}></BoolFilter>
    {:else if filteredField === CustomField.Rating}
        <RatingFilter></RatingFilter>
    {:else if filteredField === CustomField.Impression}
        <ImpressionFilter></ImpressionFilter>
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
                <BadgeTag
                    label={field}
                    striked={!value}
                    onclick={() => pageState.appliedFilters.boolFilters.delete(field)}
                ></BadgeTag>
            {/each}
            {#each pageState.appliedFilters.allowedRatings as rating}
                <BadgeTag
                    label={''}
                    striked={false}
                    onclick={() => pageState.appliedFilters.allowedRatings.delete(rating)}
                >
                    <RatingIcon {rating}></RatingIcon>
                </BadgeTag>
            {/each}
            {#each pageState.appliedFilters.allowedImpressions as impression}
                <BadgeTag
                    label={''}
                    striked={false}
                    onclick={() => pageState.appliedFilters.allowedImpressions.delete(impression)}
                >
                    <ImpressionBadge {impression}></ImpressionBadge>
                </BadgeTag>
            {/each}
        {/if}
    </div>
</div>

<style></style>
