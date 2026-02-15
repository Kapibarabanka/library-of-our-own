<script lang="ts">
    import * as Select from '$ui/select';
    import { SortBy, SortDirection } from './_types/filter-enums';
    import { filterState } from './state.svelte';
</script>

<div class="flex flex-col gap-2">
    <div id="sort-by" class="flex gap-1">
        <Select.Root type="single" bind:value={filterState.sortBy}>
            <Select.Trigger class="w-full">
                {filterState.sortBy}
            </Select.Trigger>
            <Select.Content>
                <Select.Group>
                    {#each Object.values(SortBy) as option}
                        <Select.Item value={option} label={option}></Select.Item>
                    {/each}
                </Select.Group>
            </Select.Content>
        </Select.Root>
        <Select.Root type="single" bind:value={filterState.sortDirection}>
            <Select.Trigger class="w-full">
                {filterState.sortDirection}
            </Select.Trigger>
            <Select.Content>
                <Select.Group>
                    {#each Object.values(SortDirection) as option}
                        <Select.Item value={option} label={option}></Select.Item>
                    {/each}
                </Select.Group>
            </Select.Content>
        </Select.Root>
    </div>
</div>
