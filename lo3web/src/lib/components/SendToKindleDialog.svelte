<script lang="ts">
    import { Button } from '$ui/button';
    import * as Dialog from '$ui/dialog';
    import { Spinner } from '$lib/components/ui/spinner/index.js';
    import SquareLink from 'lucide-svelte/icons/square-arrow-out-up-right';
    import { sendToKindle } from '$api/kindle.remote';
    import type { UserFicKey } from '$lib/types/domain-models';

    let {
        open = $bindable(),
        ficKey,
        ficName,
        onSent,
    }: { open: boolean; ficKey: UserFicKey; ficName: string; onSent?: () => Promise<void> } = $props();

    let confirmed = $state(false);

    function close() {
        confirmed = false;
        open = false;
    }

    async function send() {
        confirmed = true;
        await sendToKindle(ficKey);
        if (onSent) {
            await onSent();
        }
        open = false;
    }
</script>

<Dialog.Root bind:open>
    <Dialog.Content class="sm:max-w-[425px]">
        <Dialog.Header>
            <Dialog.Title class="flex flex-row gap-1">
                {#if !confirmed}
                    Please confirm
                {:else}
                    <Spinner /><span>Sending fic to Kindle</span>
                {/if}
            </Dialog.Title>
        </Dialog.Header>
        {#if !confirmed}
            Send '{ficName}' to Kindle?
        {:else}
            <span>
                The file is being downloaded and sent to your Kindle library. It will take some time, so you can close
                this dialog and check <a
                    href="https://www.amazon.com/sendtokindle"
                    target="_blank"
                    class="text-primary font-medium underline underline-offset-4"
                >
                    your Amazon content <SquareLink size="12" class="inline" />
                </a> in a couple of minutes
            </span>
        {/if}
        <Dialog.Footer>
            {#if !confirmed}
                <div class="flex gap-2 justify-between">
                    <Button variant="outline" onclick={() => close()}>Cancel</Button>
                    <Button onclick={() => send()}>Send</Button>
                </div>
            {:else}
                <Button onclick={() => close()}>Close</Button>
            {/if}
        </Dialog.Footer>
    </Dialog.Content>
</Dialog.Root>

<style></style>
