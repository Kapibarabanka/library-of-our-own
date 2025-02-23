<script lang="ts">
    import type { BoolField } from '$lib/types/filter-enums';
    import { pageState } from '../state.svelte';
    import Button, { Label } from '@smui/button';

    let { filteredField }: { filteredField: BoolField } = $props();
</script>

<div class="flex justify-around">
    <Button
        color="secondary"
        onclick={() => pageState.appliedFilters.boolFilters.set(filteredField, true)}
        variant="outlined"
    >
        <Label>Yes</Label>
    </Button>
    <Button
        color="secondary"
        onclick={() => pageState.appliedFilters.boolFilters.set(filteredField, false)}
        variant="outlined"
    >
        <Label>No</Label>
    </Button>
</div>

<style></style>
