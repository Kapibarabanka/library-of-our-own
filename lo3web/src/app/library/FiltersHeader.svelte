<script lang="ts">
    import {
        ao3Fields,
        BoolField,
        CustomField,
        filterableFields,
        FilterType,
        TagField,
        tagFieldLabels,
        TagInclusion,
        userFields,
        type FilterableField,
    } from './_types/filter-enums';
    import { filterState } from './state.svelte';
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
    import { filterIcons } from '$lib/utils/icon-utils';

    const ao3FieldsWithIcons = ao3Fields.map(f => ({ ...f, icon: filterIcons[f.field] }));
    const userFieldsWithIcons = userFields.map(f => ({ ...f, icon: filterIcons[f.field] }));
    const allFieldsWithIcons = filterableFields.map(f => ({ ...f, icon: filterIcons[f.field] }));
    const itemsByField = new Map(allFieldsWithIcons.map(item => [item.field, item]));

    let filteredField: FilterableField = $state(TagField.relationships);
    let filterType = $derived(getFilterType(filteredField));
    let selectedItem = $derived(itemsByField.get(filteredField));
</script>

<div class="flex flex-col gap-2">
    <div id="filter-type">
        <Select.Root type="single" bind:value={filteredField}>
            <Select.Trigger class="w-full">
                {#if selectedItem}
                    <div class="flex items-center">
                        <selectedItem.icon size={16} class="mr-2" /><span>{selectedItem.label}</span>
                    </div>
                {:else}
                    {filteredField}
                {/if}
            </Select.Trigger>
            <Select.Content preventScroll={true}>
                <Select.Group>
                    <Select.Label>Ao3 Data</Select.Label>
                    {#each ao3FieldsWithIcons as item}
                        <Select.Item value={item.field}>
                            <item.icon size={16} class="mr-2" /><span>{item.label}</span>
                        </Select.Item>
                    {/each}
                </Select.Group>
                <Select.Group>
                    <Select.Label>Your Data</Select.Label>
                    {#each userFieldsWithIcons as item}
                        <Select.Item value={item.field}>
                            <item.icon size={16} class="mr-2" /><span>{item.label}</span>
                        </Select.Item>
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
        {#if filterState.hasApplied}
            <Label>Applied filters:</Label>
            {#each filterState.includedTagFilters as [tagType, includedTags]}
                {#each includedTags as tag}
                    <BadgeTag
                        label={tag}
                        striked={false}
                        onclick={() => filterState.withoutTagFilter(tagType, TagInclusion.Include, tag)}
                    ></BadgeTag>
                {/each}
            {/each}
            {#each filterState.excludedTagFilters as [tagType, excludedTags]}
                {#each excludedTags as tag}
                    <BadgeTag
                        label={tag}
                        striked={true}
                        onclick={() => filterState.withoutTagFilter(tagType, TagInclusion.Exclude, tag)}
                    ></BadgeTag>
                {/each}
            {/each}
            {#each filterState.boolFilters as [field, value]}
                <BadgeTag label={field} striked={!value} onclick={() => filterState.boolFilters.delete(field)}
                ></BadgeTag>
            {/each}
            {#each filterState.allowedRatings as rating}
                <BadgeTag label={''} striked={false} onclick={() => filterState.allowedRatings.delete(rating)}>
                    <RatingIcon {rating}></RatingIcon>
                </BadgeTag>
            {/each}
            {#each filterState.allowedImpressions as impression}
                <BadgeTag label={''} striked={false} onclick={() => filterState.allowedImpressions.delete(impression)}>
                    <ImpressionBadge {impression}></ImpressionBadge>
                </BadgeTag>
            {/each}
        {/if}
    </div>
</div>

<style></style>
