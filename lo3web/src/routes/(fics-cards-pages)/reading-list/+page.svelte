<script lang="ts">
    import { goto } from '$app/navigation';
    import { pageState } from '@app/library/state.svelte';
    import { BoolField } from '@app/library/_types/filter-enums';
    pageState.clearFilters();
    pageState.appliedFilters.boolFilters.set(BoolField.Backlog, true);
    goto('/library');
</script>
