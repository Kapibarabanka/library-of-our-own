<script lang="ts">
    import { TagInclusion, type TagField } from '../_types/filter-enums';
    import * as Select from '$ui/select';
    import { tick } from 'svelte';
    import * as Command from '$lib/components/ui/command/index.js';
    import * as Popover from '$lib/components/ui/popover/index.js';
    import { buttonVariants } from '$lib/components/ui/button/index.js';
    import ChevronDown from 'lucide-svelte/icons/chevron-down';
    import { pageState } from '../state.svelte';
    import { ScrollArea } from '$ui/scroll-area';
    import Input from '$ui/input/input.svelte';
    import { Label } from '$ui/label';
    import Button from '$ui/button/button.svelte';
    let { filteredField }: { filteredField: TagField } = $props();
    let tagInclusion = $state(TagInclusion.Include);

    let openPopover = $state(false);
    let allFilterItems = $derived([...(pageState.tagFilters.get(filteredField) ?? [])]);
    const count = 50;
    let visibleItems = $derived(allFilterItems.slice(0, count));
    const popoverId = 'tag-selection-popover';

    function handleSelection(tag: string) {
        pageState.withTagFilter(filteredField, tagInclusion, tag);
        openPopover = false;
        tick().then(() => {
            document.getElementById(popoverId)?.focus();
        });
    }
</script>

<div class="flex flex-col gap-1">
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
                            {#each visibleItems as tagFilterItem}
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
    <div>
        <Label for="tags" class="text-center text-xs text-muted-foreground">
            Showing top {count}
            {filteredField.toLowerCase()}s, filter to see more
        </Label>
        <ScrollArea id="tags" class="h-52 rounded-md border" type="always">
            <div class="p-1 flex flex-col">
                {#each visibleItems as tagFilterItem}
                    <!-- <div
                        class="cursor-pointer items-center rounded-sm px-2 py-1.5 text-sm outline-none hover:bg-accent hover:text-accent-foreground"
                        onclick={() => handleSelection(tagFilterItem.value)}
                    >
                        {tagFilterItem.label}
                    </div> -->
                    <!-- <Button variant="ghost" onclick={() => handleSelection(tagFilterItem.value)}>
                        {tagFilterItem.label}
                    </Button> -->
                    <button
                        class="cursor-pointer text-start rounded-sm px-2 py-1.5 text-sm hover:bg-accent hover:text-accent-foreground"
                        onclick={() => handleSelection(tagFilterItem.value)}>{tagFilterItem.label}</button
                    >
                {/each}
            </div>
        </ScrollArea>
    </div>
</div>

<style></style>
