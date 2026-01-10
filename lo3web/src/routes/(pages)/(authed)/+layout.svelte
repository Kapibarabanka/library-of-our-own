<script lang="ts">
    import AppSidebar from '$lib/components/AppSidebar.svelte';
    import ParseDialog from '$lib/components/ParseDialog.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import Button from '$ui/button/button.svelte';
    import { Toaster } from '$ui/sonner';
    import '../../../app.css';

    let { children } = $props();
    let parseDialogOpen = $state(false);
</script>

<Toaster />

<Sidebar.Provider>
    <AppSidebar />
    <main class="w-full">
        <div class="flex gap-2 justify-between items-center py-3 pr-2">
            <div class="flex gap-2 items-center sticky top-0 bg-white">
                <Sidebar.Trigger size="icon" />
                <h4 class="scroll-m-20 text-xl font-semibold tracking-tight">Library of our own</h4>
            </div>
            <Button size="sm" onclick={() => (parseDialogOpen = true)}>Parse fic</Button>
        </div>
        {@render children?.()}
        <ParseDialog bind:open={parseDialogOpen} />
    </main>
</Sidebar.Provider>
