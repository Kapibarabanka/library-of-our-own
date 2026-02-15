<script lang="ts">
    import { TagInclusion, type TagField } from '../_types/filter-enums';
    import * as Select from '$ui/select';
    import { Label } from '$ui/label';
    import { Input } from '$lib/components/ui/input';
    import { filterState, pageState } from '../state.svelte';
    let { filteredField }: { filteredField: TagField } = $props();
    let tagInclusion = $state(TagInclusion.Include);

    let allFilterItems = $derived([...(pageState.tagFilters.get(filteredField) ?? [])]);
    const count = 50;
    let filterValue = $state('');
    let filteredValues = $derived.by(() => {
        if (filterValue == null || filterValue === '') {
            return allFilterItems;
        }
        const lowerFilter = filterValue.toLocaleLowerCase();
        return allFilterItems.filter(i => i.lowercase.includes(lowerFilter));
    });
    let visibleItems = $derived(filteredValues.slice(0, count));

    function handleSelection(tag: string) {
        filterValue = '';
        filterState.withTagFilter(filteredField, tagInclusion, tag);
    }
</script>

<div class="flex flex-col gap-1">
    <div class="flex gap-1">
        <Select.Root type="single" bind:value={tagInclusion}>
            <Select.Trigger class="w-[110px]">
                {tagInclusion}
            </Select.Trigger>
            <Select.Content>
                <Select.Group>
                    <Select.Item value={TagInclusion.Include} label={TagInclusion.Include}></Select.Item>
                    <Select.Item value={TagInclusion.Exclude} label={TagInclusion.Exclude}></Select.Item>
                </Select.Group>
            </Select.Content>
        </Select.Root>
        <Input class="flex-1 text-sm" placeholder="Type to filter.." bind:value={filterValue}></Input>
    </div>
    <div>
        {#if filteredValues.length > count}
            <Label for="tags" class="text-center text-xs text-muted-foreground">
                Showing top {count}
                {filteredField.toLowerCase()}s, filter to see more
            </Label>
        {/if}
        <div class="p-1 flex flex-col max-h-52 h-fit rounded-md border overflow-y-scroll">
            {#if allFilterItems.length === 0}
                <div class="text-center">No other {filteredField.toLowerCase()}s availible</div>
            {:else}
                {#each visibleItems as tagFilterItem}
                    <button
                        class="cursor-pointer text-start rounded-sm px-2 py-1.5 text-sm hover:bg-accent hover:text-accent-foreground"
                        onclick={() => handleSelection(tagFilterItem.value)}>{tagFilterItem.label}</button
                    >
                {/each}
            {/if}
        </div>
    </div>
</div>

<style></style>
