<script lang="ts">
    import { goto } from '$app/navigation';
    import type { FicCardData, UserFicKey } from '$lib/types/domain-models';
    import { Button } from '$ui/button';
    import * as Card from '$ui/card';
    import Maximize from 'lucide-svelte/icons/maximize-2';
    import BookCheck from 'lucide-svelte/icons/book-check';

    let { fic, onFinish }: { fic: FicCardData; onFinish: (key: UserFicKey) => void } = $props();
</script>

<Card.Root>
    <Card.Content class="flex gap-2 flex-row p-3">
        <div class="flex-1">
            <Card.Title>
                <a href={fic.ao3Info.link}>{fic.ao3Info.title}</a>
            </Card.Title>
            <Card.Description>
                <span class="text-xs">{fic.ao3Info.relationships?.join(', ')} </span>
            </Card.Description>
        </div>
        <div class="flex flex-col justify-between gap-3 items-end">
            <Button
                variant="ghost"
                class="p-1 h-4"
                onclick={() => goto(`/fic/${fic.key.ficType.toLowerCase()}-${fic.key.ficId}`)}
            >
                <Maximize class="text-muted-foreground" size={15}></Maximize>
            </Button>
            <Button size="sm" variant="outline" onclick={() => onFinish(fic.key)}><BookCheck />Finish</Button>
        </div>
    </Card.Content>
</Card.Root>

<style></style>
