<script lang="ts">
    import { TagInclusion, TagField, type TagFilterItem } from '$lib/types/filter-enums';
    import Select, { Option } from '@smui/select';
    import Textfield from '@smui/textfield';
    import Autocomplete from '@smui-extra/autocomplete';
    import { pageState } from '../state.svelte';
    import Menu from '@smui/menu';
    import { Anchor } from '@smui/menu-surface';

    let { filteredField }: { filteredField: TagField } = $props();
    let tagInclusion = $state(TagInclusion.Include);
    let autocompleteValue: TagFilterItem | undefined = $state();
    let autocompleteText = $state('');
    let selector: Autocomplete;
    let options = [...(pageState.tagFilters.get(TagField.Ship) ?? [])];
    let menu: Menu;
    let anchor: HTMLDivElement | undefined = $state();
    // let options = $state([
    //     { value: 'aa', count: 1, label: 'aa 1' },
    //     { value: 'bb', count: 2, label: 'bb 2' },
    // ]);
    function handleSelection(event: CustomEvent<TagFilterItem>) {
        // Don't actually select the item.
        event.preventDefault();
        selector.blur();
        console.log(event.detail);
    }
    function handleClose() {
        console.log('CLOSE');
        autocompleteValue = undefined;
        autocompleteText = '';
        selector.blur();
    }
</script>

<div class="flex gap-1">
    <div style="width: 120px;">
        <Select bind:value={tagInclusion} style="width: 100%">
            <Option value={TagInclusion.Include}>{TagInclusion.Include}</Option>
            <Option value={TagInclusion.Exclude}>{TagInclusion.Exclude}</Option>
        </Select>
    </div>
    <div class="flex-1">
        <Autocomplete
            bind:this={selector}
            onSMUIAutocompleteSelected={handleSelection}
            onSMUIMenuSurfaceClosed={handleClose}
            menu$anchor={false}
            menu$anchorCorner="BOTTOM_START"
            class="tag-autocomplete"
            getOptionLabel={(option: TagFilterItem | undefined) => option?.label ?? ''}
            {options}
            bind:value={autocompleteValue}
            bind:text={autocompleteText}
            style="width: 100%;"
        >
            <Textfield
                label={`Select ${filteredField.toLowerCase()}`}
                bind:value={autocompleteText}
                style="width: 100%;"
            />
        </Autocomplete>
    </div>
</div>

<style>
</style>
