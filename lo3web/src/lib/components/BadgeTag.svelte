<script lang="ts">
    import Badge from '$ui/badge/badge.svelte';
    const { label, striked, onclick }: { label: string; striked: boolean; onclick: () => void } = $props();
</script>

<Badge class="text-sm font-light inline-flex mr-1 mt-1" variant="outline" {onclick}
    >{#if striked}
        <s>{label}</s>{'  '}✕
    {:else}
        {label}{'  '}✕
    {/if}
</Badge>

<style></style>
