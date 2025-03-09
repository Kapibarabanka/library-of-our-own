<script lang="ts">
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { useSidebar } from '$lib/components/ui/sidebar/index.js';
    import { BoolField } from '@app/library/_types/filter-enums';
    import { pageState as libraryState } from '@app/library/state.svelte';
    const sidebar = useSidebar();

    function toLibrary(field: BoolField | null) {
        libraryState.clearFilters();
        if (field) {
            libraryState.appliedFilters.boolFilters.set(field, true);
        }
        sidebar.toggle();
    }
</script>

<Sidebar.Root>
    <Sidebar.Header>Hello Kapibarabanka</Sidebar.Header>
    <Sidebar.Content>
        <Sidebar.Group>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => sidebar.toggle()}>
                            {#snippet child({ props })}
                                <a href="/" {...props}>Home</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(null)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}>All Fics</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(BoolField.Backlog)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}>Reading List</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(BoolField.OnKindle)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}>Fics on Kindle</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
    </Sidebar.Content>
    <Sidebar.Footer />
</Sidebar.Root>
