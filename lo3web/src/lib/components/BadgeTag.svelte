<script lang="ts">
    import Badge from '$ui/badge/badge.svelte';
    import type { Snippet } from 'svelte';
    const {
        label,
        striked,
        onclick,
        children,
    }: { label: string; striked: boolean; onclick: () => void; children?: Snippet } = $props();
</script>

<Badge class="text-sm font-light inline-flex mr-1 mt-1" variant="outline" {onclick}>
    {@render children?.()}
    {#if striked}
        <s>{label}</s>
    {:else}
        {label}
    {/if}
    <span class="ml-2">✕</span>
</Badge>

<style></style>
