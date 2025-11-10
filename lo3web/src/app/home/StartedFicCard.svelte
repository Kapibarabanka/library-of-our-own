<script lang="ts">
    import type { FicCardData } from '$lib/types/domain-models';
    import { Button } from '$ui/button';
    import * as Item from '$ui/item';
    import BookCheck from 'lucide-svelte/icons/book-check';

    let { fic, onFinish }: { fic: FicCardData; onFinish: (fic: FicCardData) => void } = $props();
</script>

<Item.Root variant="outline" class="bg-card rounded-xl border shadow">
    <Item.Content>
        <Item.Title class="text-base font-semibold">
            <a href={`/fic/${fic.key.ficType.toLowerCase()}-${fic.key.ficId}`}>{fic.ao3Info.title}</a>
        </Item.Title>
        <span class="text-xs text-muted-foreground">{fic.ao3Info.relationships?.join(', ')}</span>
    </Item.Content>
    <Item.Actions>
        <Button size="sm" variant="outline" onclick={() => onFinish(fic)}><BookCheck />Finish</Button>
    </Item.Actions>
</Item.Root>

<style></style>
