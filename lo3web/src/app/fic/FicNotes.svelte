<script lang="ts">
    import { addNote } from '$api/fics-details.remote';
    import type { Fic } from '$lib/types/domain-models';
    import { getUserFicKey } from '$lib/utils/fic-utils';
    import { formatDateTime } from '$lib/utils/label-utils';
    import { Button } from '$ui/button';
    import * as Card from '$ui/card';
    import { Textarea } from '$ui/textarea';
    import { Plus } from '@lucide/svelte';

    let { fic = $bindable() }: { fic: Fic } = $props();

    let addingNote = $state(false);
    let newNote = $state<string | undefined>(undefined);
    let key = $derived(getUserFicKey(fic));

    function finishAdding() {
        newNote = undefined;
        addingNote = false;
    }

    async function submit() {
        if (newNote) {
            const addedNote = await addNote({ key, text: newNote });
            fic = { ...fic, notes: [addedNote, ...fic.notes] };
        }
        finishAdding();
    }
</script>

<div class="flex flex-col gap-2">
    {#if !addingNote}
        <Button variant="outline" class="md:w-fit" onclick={() => (addingNote = true)}><Plus />Add Note</Button>
    {:else}
        <Card.Root>
            <Card.Header>
                <Card.Title>New note</Card.Title>
            </Card.Header>
            <Card.Content>
                <Textarea
                    bind:value={newNote}
                    class="text-sm min-h-[200px]"
                    id="note"
                    placeholder="write your note here"
                />
            </Card.Content>
            <Card.Footer class="flex-row justify-end gap-3">
                <Button variant="outline" onclick={() => finishAdding()}>Cancel</Button>
                <Button variant="default" onclick={() => submit()} disabled={!newNote}>Save</Button>
            </Card.Footer>
        </Card.Root>
    {/if}
    {#each fic.notes ?? [] as note}
        <Card.Root>
            <Card.Header>
                <Card.Title>{formatDateTime(note.date + 'Z')}</Card.Title>
            </Card.Header>
            <Card.Content>{note.text}</Card.Content>
        </Card.Root>
    {/each}
</div>

<style></style>
