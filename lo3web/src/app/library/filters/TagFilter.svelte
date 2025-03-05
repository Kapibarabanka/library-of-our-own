<script lang="ts">
    import { TagInclusion, type TagField } from '../_types/filter-enums';
    import * as Select from '$ui/select';
    import { tick } from 'svelte';
    import * as Command from '$lib/components/ui/command/index.js';
    import * as Popover from '$lib/components/ui/popover/index.js';
    import { buttonVariants } from '$lib/components/ui/button/index.js';
    import ChevronDown from 'lucide-svelte/icons/chevron-down';
    import { pageState } from '../state.svelte';

    let { filteredField }: { filteredField: TagField } = $props();
    let tagInclusion = $state(TagInclusion.Include);

    let openPopover = $state(false);
    // TODO: add virtual scrolling, now Tag is loading very slowly
    let tagFilterItems = $derived([...(pageState.tagFilters.get(filteredField) ?? [])]);
    const popoverId = 'tag-selection-popover';

    function handleSelection(tag: string) {
        pageState.withTagFilter(filteredField, tagInclusion, tag);
        openPopover = false;
        tick().then(() => {
            document.getElementById(popoverId)?.focus();
        });
    }
</script>

<div class="flex gap-1">
    <Select.Root type="single" name="favoriteFruit" bind:value={tagInclusion}>
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
    <Popover.Root bind:open={openPopover}>
        <Popover.Trigger
            class={buttonVariants({
                variant: 'outline',
                class: 'flex-1 flex justify-between text-muted-foreground',
            })}
            id={popoverId}
        >
            Select a {filteredField.toLowerCase()}
            <ChevronDown class="opacity-50" />
        </Popover.Trigger>
        <Popover.Content class="p-0" align="end" side="bottom">
            <Command.Root>
                <Command.Input placeholder="Search..." />
                <Command.List>
                    <Command.Empty>No results found.</Command.Empty>
                    <Command.Group>
                        {#each tagFilterItems as tagFilterItem}
                            <Command.Item
                                value={tagFilterItem.value}
                                onSelect={() => handleSelection(tagFilterItem.value)}
                            >
                                {tagFilterItem.label}
                            </Command.Item>
                        {/each}
                    </Command.Group>
                </Command.List>
            </Command.Root>
        </Popover.Content>
    </Popover.Root>
</div>

<style></style>
