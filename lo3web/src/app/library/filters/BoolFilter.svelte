<script lang="ts">
    import { Button } from '$ui/button';
    import type { BoolField } from '../_types/filter-enums';
    import { pageState } from '../state.svelte';

    let { filteredField }: { filteredField: BoolField } = $props();
</script>

<div class="flex justify-around">
    <Button
        class="w-[100px]"
        onclick={() => pageState.appliedFilters.boolFilters.set(filteredField, false)}
        variant="outline">No</Button
    >
    <Button
        class="w-[100px]"
        onclick={() => pageState.appliedFilters.boolFilters.set(filteredField, true)}
        variant="outline">Yes</Button
    >
</div>

<style></style>
