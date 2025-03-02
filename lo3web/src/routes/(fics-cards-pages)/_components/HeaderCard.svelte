<script lang="ts">
    import type { Snippet } from 'svelte';
    import * as Card from '$ui/card';
    import { Label } from '$ui/label';

    const { title, children }: { title: string; children: Snippet } = $props();
</script>

<Card.Root>
    <Card.Content class="p-2 flex flex-col gap-1">
        <Label class="ml-1">{title}</Label>
        {@render children()}
    </Card.Content>
</Card.Root>

<style></style>
