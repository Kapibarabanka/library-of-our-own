<script lang="ts">
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { useSidebar } from '$lib/components/ui/sidebar/index.js';
    import { BoolField } from '@app/library/_types/filter-enums';
    import { pageState as libraryState } from '@app/library/state.svelte';
    import House from 'lucide-svelte/icons/house';
    import Tablet from 'lucide-svelte/icons/tablet';
    import Todo from 'lucide-svelte/icons/list-todo';
    import Library from 'lucide-svelte/icons/library-big';
    import Hash from 'lucide-svelte/icons/hash';
    import Heart from 'lucide-svelte/icons/heart';
    import Earth from 'lucide-svelte/icons/earth';
    import LogOut from 'lucide-svelte/icons/log-out';
    import { globalState } from '@app/global-state.svelte';
    const sidebar = useSidebar();

    function toLibrary(field: BoolField | null) {
        libraryState.clearFilters();
        if (field) {
            libraryState.appliedFilters.boolFilters.set(field, true);
        }
        sidebar.toggle();
    }

    const statsPages = [
        {
            title: 'Ships Stats',
            url: 'ships',
            icon: Heart,
        },
        {
            title: 'Fandoms Stats',
            url: 'fandoms',
            icon: Earth,
        },
        {
            title: 'Tags Stats',
            url: 'tags',
            icon: Hash,
        },
    ];
</script>

<Sidebar.Root>
    <Sidebar.Header>
        <Sidebar.Menu>
            <Sidebar.MenuItem>
                <Sidebar.MenuButton>Hello, {globalState.user?.name}!</Sidebar.MenuButton>
            </Sidebar.MenuItem>
            <Sidebar.MenuItem>
                <Sidebar.MenuButton onclick={() => sidebar.toggle()}>
                    {#snippet child({ props })}
                        <a href="/logout" {...props}><LogOut />Log Out</a>
                    {/snippet}
                </Sidebar.MenuButton>
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
        <Sidebar.Group>
            <Sidebar.GroupLabel>Stats</Sidebar.GroupLabel>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    {#each statsPages as statPage (statPage.title)}
                        <Sidebar.MenuButton onclick={() => sidebar.toggle()}>
                            {#snippet child({ props })}
                                <a href={'/stats/' + statPage.url} {...props}><statPage.icon />{statPage.title}</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    {/each}
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
    </Sidebar.Content>
    <Sidebar.Footer />
</Sidebar.Root>
