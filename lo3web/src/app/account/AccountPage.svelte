<script lang="ts">
    import type { User } from '$lib/types/ui-models';
    import * as Item from '$lib/components/ui/item/index.js';
    import * as Dialog from '$lib/components/ui/dialog/index.js';
    import { Button, buttonVariants } from '$ui/button';
    import SectionHeader from '$lib/components/SectionHeader.svelte';
    import Input from '$ui/input/input.svelte';
    import { setKindleEmail } from '$api/user.remote';
    import { getContext } from 'svelte';

    let user = getContext<User>('user');
    let email = $state(user.kindleEmail);
    let emailSet = $derived(email != null && email !== '');

    let dialogOpen = $state(false);

    function cancel() {
        email = user.kindleEmail;
        dialogOpen = false;
    }
    async function save() {
        await setKindleEmail(email);
        user.kindleEmail = email;
        dialogOpen = false;
    }
</script>

<SectionHeader text="Account Settings" />
<Item.Root variant="outline" size="sm">
    <Item.Content>
        <Item.Title>Your Kindle email is {emailSet ? 'set' : 'not set'}</Item.Title>
    </Item.Content>
    <Item.Actions>
        <Dialog.Root bind:open={dialogOpen}>
            <Dialog.Trigger class={buttonVariants({ variant: 'outline', size: 'sm' })}>
                {emailSet ? 'Change' : 'Set'}
            </Dialog.Trigger>
            <Dialog.Content class="sm:max-w-[425px]">
                <Dialog.Header>
                    <Dialog.Title>Enter your Kindle email</Dialog.Title>
                </Dialog.Header>
                <Input class="flex-1 text-sm" placeholder="Type here" bind:value={email}></Input>
                <Dialog.Footer>
                    <Button variant="outline" onclick={() => cancel()}>Cancel</Button>
                    <Button onclick={() => save()}>Save</Button>
                </Dialog.Footer>
            </Dialog.Content>
        </Dialog.Root>
    </Item.Actions>
</Item.Root>

<style></style>
