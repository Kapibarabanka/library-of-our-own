<script lang="ts">
    import type { Snippet } from 'svelte';
    import * as Item from '$ui/item';

    const { title, children }: { title: string; children: Snippet } = $props();
</script>

<Item.Root variant="muted" size="sm">
    <Item.Content>
        <Item.Title>
            {title}
        </Item.Title>
        {@render children()}
    </Item.Content>
</Item.Root>

<style></style>
