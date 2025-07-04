<script lang="ts">
    import AppSidebar from '$lib/components/AppSidebar.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { globalState } from '@app/global-state.svelte';
    import '../../app.css';
    let { data, children } = $props();
    globalState.user = data.user;
</script>

<Sidebar.Provider>
    <AppSidebar />
    <main class="w-full">
        <div class="flex gap-2 items-center sticky top-0 bg-white">
            <Sidebar.Trigger size="icon" />
            <h4 class="scroll-m-20 text-xl font-semibold tracking-tight">Library of our own</h4>
        </div>
        {@render children?.()}
    </main>
</Sidebar.Provider>
