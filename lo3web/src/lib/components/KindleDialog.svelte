<script lang="ts">
    import { Button } from '$ui/button';
    import * as Dialog from '$ui/dialog';
    import { Spinner } from '$lib/components/ui/spinner/index.js';
    import SquareLink from 'lucide-svelte/icons/square-arrow-out-up-right';
    import { sendToKindle } from '$api/kindle.remote';
    import type { FicDetails, UserFicKey } from '$lib/types/domain-models';
    import { patchDetails } from '$api/fics-details.remote';

    let {
        open = $bindable(),
        ficKey,
        details,
        ficName,
        onSubmitted,
    }: {
        open: boolean;
        ficKey: UserFicKey;
        details: FicDetails;
        ficName: string;
        onSubmitted?: () => Promise<void>;
    } = $props();

    let isOnKindle = $derived(details.isOnKindle);
    let stateKey = $derived(ficKey.ficId + isOnKindle.toString());
    let confirmed = $state(false);

    function close() {
        confirmed = false;
        open = false;
    }

    async function confirm() {
        if (isOnKindle) {
            await patchDetails({
                key: ficKey,
                details: { ...details, isOnKindle: false },
            });
        } else {
            confirmed = true;
            await sendToKindle(ficKey);
        }
        if (onSubmitted) {
            await onSubmitted();
        }
        close();
    }
</script>

<Dialog.Root bind:open>
    <Dialog.Content class="sm:max-w-[425px]">
        {#key stateKey}
            <Dialog.Header>
                <Dialog.Title class="flex flex-row gap-1">
                    {#if !confirmed}
                        Please confirm
                    {:else}
                        <Spinner /><span>Sending fic to Kindle</span>
                    {/if}
                </Dialog.Title>
            </Dialog.Header>
            {#if isOnKindle}
                This action won't remove the document from your Kindle library, just mark it as "Not on Kindle" in this
                site's database.
            {:else if !confirmed}
                Send '{ficName}' to Kindle?
            {:else}
                <span>
                    The file is being downloaded and sent to your Kindle library. It will take some time, so you can
                    close this dialog and check <a
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
                    <Button onclick={() => confirm()}>Confirm</Button>
                    <Button variant="outline" onclick={() => close()}>Cancel</Button>
                {:else}
                    <Button onclick={() => close()}>Close</Button>
                {/if}
            </Dialog.Footer>
        {/key}
    </Dialog.Content>
</Dialog.Root>

<style></style>
