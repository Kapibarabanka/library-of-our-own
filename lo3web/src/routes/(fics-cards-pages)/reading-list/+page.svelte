<script lang="ts">
    import { goto } from '$app/navigation';
    import { pageState } from '../_components/state.svelte';
    import { BoolField } from '../_types/filter-enums';
    pageState.clearFilters();
    pageState.appliedFilters.boolFilters.set(BoolField.Backlog, true);
    goto('/library');
</script>
