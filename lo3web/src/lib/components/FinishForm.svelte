<script lang="ts">
    import { Label } from '$ui/label';
    import { UserImpression, type FicDetails, type UserFicKey } from '$lib/types/domain-models';
    import * as Sheet from '$lib/components/ui/sheet';
    import { Button } from '$ui/button';
    import { Checkbox } from '$ui/checkbox';
    import Textarea from '$ui/textarea/textarea.svelte';
    import LoaderCircle from 'lucide-svelte/icons/loader-circle';
    import type { FinishInfo } from '$lib/types/api-models';
    import { finishFic } from '$api/fics-details.remote';
    import ImpressionInput from './ImpressionInput.svelte';

    let { key, details, onSubmitted }: { key: UserFicKey; details: FicDetails; onSubmitted: () => void } = $props();

    let isLoading = $state(false);

    let abandoned = $state(false);
    let spicy = $state(details.spicy);
    let impression = $state<UserImpression | ''>(details.impression ?? '');
    let note = $state<string | undefined>(undefined);

    async function submit() {
        isLoading = true;
        const finishInfo: FinishInfo = {
            key,
            abandoned,
            spicy,
            impression: !impression ? undefined : impression,
            note,
        };
        try {
            await finishFic(finishInfo);
        } catch (e) {
            alert(e);
        } finally {
            isLoading = false;
        }
        onSubmitted();
    }
</script>

<Sheet.Content side="top" class="p-3">
    {#if isLoading}
        <div class="flex h-[280px] items-center text-muted-foreground">
            <LoaderCircle size={80} class="animate-spin flex-1" />
        </div>
    {:else}
        <Sheet.Header>
            <Sheet.Title>How was this fic?</Sheet.Title>
        </Sheet.Header>
        <div class="flex flex-col gap-3 py-4">
            <div class="flex items-center space-x-2">
                <Checkbox id="abandoned" bind:checked={abandoned} />
                <Label for="abandoned">
                    <span class="text-sm font-medium leading-none">Abandoned</span>
                    <span class="text-muted-foreground text-xs">(fic won't be included in statistics)</span>
                </Label>
            </div>
            <div class="flex items-center space-x-2">
                <Checkbox id="spicy" bind:checked={spicy} />
                <Label for="spicy">
                    <span class="text-sm font-medium leading-none">🔥 Spicy</span>
                </Label>
            </div>
            <ImpressionInput bind:impression></ImpressionInput>
            <Textarea bind:value={note} class="text-sm" id="note" placeholder="Add a note if you want to" />
        </div>
        <Sheet.Footer>
            <Button onclick={async () => await submit()}>Mark as finished</Button>
        </Sheet.Footer>
    {/if}
</Sheet.Content>
