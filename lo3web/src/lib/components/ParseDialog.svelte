<script lang="ts">
    import { Button } from '$ui/button';
    import * as Dialog from '$ui/dialog';
    import { Spinner } from '$lib/components/ui/spinner/index.js';
    import { Input } from '$ui/input';
    import { getFicByLink } from '$api/fics.remote';
    import { goto } from '$app/navigation';
    import type { RecoverableError } from '$api/errors-utils';

    let {
        open = $bindable(),
        onSubmitted,
    }: {
        open: boolean;
        onSubmitted?: () => Promise<void>;
    } = $props();

    let inputValue = $state('');
    let loading = $state(false);
    let error = $state<string | undefined>(undefined);

    function onOpenChange(v: boolean) {
        if (!v) {
            close();
        }
    }

    function close() {
        open = false;
        inputValue = '';
        loading = false;
        error = undefined;
    }

    async function parse() {
        loading = true;
        try {
            const response = await getFicByLink(inputValue);
            if (response.fic) {
                goto(`/fic/${response.fic.ao3Info.ficType.toLowerCase()}-${response.fic.ao3Info.id}`);
                close();
            } else {
                loading = false;
                error = response.error?.defaultMessage;
            }
        } catch (e) {
            loading = false;
            error = 'Unexpected error happened. If possible, report it through the bot:\n' + e;
            alert(e);
        }
    }
</script>

<Dialog.Root bind:open {onOpenChange}>
    <Dialog.Content class="sm:max-w-[425px]">
        {#if loading}
            <Dialog.Header>
                <Dialog.Title class="flex flex-row gap-1"><Spinner /><span>Parsing fic</span></Dialog.Title>
            </Dialog.Header>
            <span>
                This process can take several minutes. When it's finished the fic will appear at the top of your
                <a href="/library" target="_self" class="text-primary font-medium underline underline-offset-4">
                    library
                </a>. If possible, don't close this dialog to see any potential errors in the parsing process.
            </span>
        {:else if error}
            <Dialog.Header>
                <Dialog.Title class="flex flex-row gap-1">Parsing failed</Dialog.Title>
            </Dialog.Header>
            <div>
                {error}
            </div>
        {:else}
            <Dialog.Header>
                <Dialog.Title class="flex flex-row gap-1">Paste the link to the fic</Dialog.Title>
            </Dialog.Header>
            <Dialog.Description>
                If the fic is a restricted work, instead paste a link to its HTML download
            </Dialog.Description>
            <Input class="flex-1 text-sm" placeholder="Paste link here" bind:value={inputValue}></Input>
            <Dialog.Footer>
                <div class="flex gap-2 justify-between">
                    <Button variant="outline" onclick={() => close()}>Cancel</Button>
                    <Button onclick={() => parse()}>Parse</Button>
                </div>
            </Dialog.Footer>{/if}
    </Dialog.Content>
</Dialog.Root>

<style></style>
