<script lang="ts">
    import AppSidebar from '$lib/components/AppSidebar.svelte';
    import ParseDialog from '$lib/components/ParseDialog.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import Button from '$ui/button/button.svelte';
    import { Toaster } from '$ui/sonner';
    import { setContext } from 'svelte';
    import '../../../app.css';

    let { children, data } = $props();
    let parseDialogOpen = $state(false);
    setContext('user', data.user);
</script>

<Toaster />

<Sidebar.Provider>
    <AppSidebar />
    <main class="w-full">
        <div class="flex gap-2 justify-between items-center p-4 pl-2 pb-0">
            <div class="flex gap-2 items-center sticky top-0 bg-white">
                <Sidebar.Trigger size="icon" />
                <h4 class="scroll-m-20 text-xl font-semibold tracking-tight">Library of our own</h4>
            </div>
            <Button size="sm" onclick={() => (parseDialogOpen = true)}>Parse fic</Button>
        </div>
        <div class="p-4">
            {@render children?.()}
        </div>
        <ParseDialog bind:open={parseDialogOpen} />
    </main>
</Sidebar.Provider>
