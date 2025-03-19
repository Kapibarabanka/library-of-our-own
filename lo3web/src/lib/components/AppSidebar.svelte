<script lang="ts">
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { useSidebar } from '$lib/components/ui/sidebar/index.js';
    import { BoolField } from '@app/library/_types/filter-enums';
    import { pageState as libraryState } from '@app/library/state.svelte';
    import House from 'lucide-svelte/icons/house';
    import Tablet from 'lucide-svelte/icons/tablet';
    import Todo from 'lucide-svelte/icons/list-todo';
    import Library from 'lucide-svelte/icons/library-big';
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
    <Sidebar.Header>
        <Sidebar.Menu>
            <Sidebar.MenuItem>
                <Sidebar.MenuButton>Hello, Kapibarabanka!</Sidebar.MenuButton>
            </Sidebar.MenuItem>
        </Sidebar.Menu>
    </Sidebar.Header>
    <Sidebar.Content>
        <Sidebar.Group>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => sidebar.toggle()}>
                            {#snippet child({ props })}
                                <a href="/" {...props}><House />Home</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
        <Sidebar.Group>
            <Sidebar.GroupLabel>Fic lists</Sidebar.GroupLabel>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(null)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}><Library />All Fics</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(BoolField.Backlog)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}><Todo />Reading List</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => toLibrary(BoolField.OnKindle)}>
                            {#snippet child({ props })}
                                <a href="/library" {...props}><Tablet />Fics on Kindle</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
    </Sidebar.Content>
    <Sidebar.Footer />
</Sidebar.Root>
