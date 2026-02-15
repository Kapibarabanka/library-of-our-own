<script lang="ts">
    import { TagInclusion, type TagField } from '../_types/filter-enums';
    import * as Tabs from '$ui/tabs';
    import { Label } from '$ui/label';
    import { Input } from '$lib/components/ui/input';
    import { filterState, pageState } from '../state.svelte';
    import Badge from '$ui/badge/badge.svelte';

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
        <Tabs.Root id="impression" bind:value={tagInclusion}>
            <Tabs.List>
                <Tabs.Trigger value={TagInclusion.Include}>{TagInclusion.Include}</Tabs.Trigger>
                <Tabs.Trigger value={TagInclusion.Exclude}>{TagInclusion.Exclude}</Tabs.Trigger>
            </Tabs.List>
        </Tabs.Root>
        <Input class="flex-1 text-sm" placeholder="Type to filter.." bind:value={filterValue}></Input>
    </div>
    <div>
        {#if filteredValues.length > count}
            <Label for="tags" class="text-center text-xs text-muted-foreground mb-1 mt-2">
                Showing top {count}
                {filteredField.toLowerCase()}, filter to see more
            </Label>
        {/if}
        <div class="p-1 flex flex-col max-h-52 h-fit rounded-md border overflow-y-scroll">
            {#if allFilterItems.length === 0}
                <div class="text-center">No other {filteredField.toLowerCase()}s availible</div>
            {:else}
                {#each visibleItems as tagFilterItem}
                    <button
                        class="cursor-pointer text-start rounded-sm px-2 py-1.5 text-sm hover:bg-accent hover:text-accent-foreground"
                        onclick={() => handleSelection(tagFilterItem.value)}
                        >{tagFilterItem.value}
                        <Badge class="h-5 min-w-5 rounded-full px-1 font-medium" variant="outline"
                            >{tagFilterItem.count}</Badge
                        ></button
                    >
                {/each}
            {/if}
        </div>
    </div>
</div>

<style></style>
