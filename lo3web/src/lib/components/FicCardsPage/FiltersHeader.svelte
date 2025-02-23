<script lang="ts">
    import Select, { Option } from '@smui/select';
    import { BoolField, filterableFields, FilterType, TagField, type FilterableField } from '$lib/types/filter-enums';
    import { getFilterType } from '$lib/utils/filter-utils';
    import { pageState } from './state.svelte';
    import Tag from '$lib/components/Tag.svelte';
    import BoolFilter from './Filters/BoolFilter.svelte';
    import TagFilter from './Filters/TagFilter.svelte';

    let filteredField: FilterableField = $state(TagField.Ship);
    let filterType = $derived(getFilterType(filteredField));
</script>

<Select bind:value={filteredField} label="Filter type">
    {#each filterableFields as filterableField}
        <Option value={filterableField}>{filterableField}</Option>
    {/each}
</Select>
{#if filterType === FilterType.Tag}
    <TagFilter filteredField={filteredField as TagField}></TagFilter>
{:else if filterType === FilterType.Bool}
    <BoolFilter filteredField={filteredField as BoolField}></BoolFilter>
{/if}
<div>
    {#if pageState.hasApplied}
        <div>Applied filters (tap filter to remove it):</div>
        {#each pageState.appliedFilters.boolFilters as [field, value]}
            <Tag
                label={`${field}: ${value ? 'Yes' : 'No'}`}
                withCross={true}
                onclick={() => pageState.appliedFilters.boolFilters.delete(field)}
            ></Tag>
        {/each}
    {/if}
</div>

<style></style>
