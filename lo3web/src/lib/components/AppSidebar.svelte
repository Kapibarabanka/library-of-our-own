<script lang="ts">
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { useSidebar } from '$lib/components/ui/sidebar/index.js';
    import { BoolField } from '@app/library/_types/filter-enums';
    import { filterState } from '@app/library/state.svelte';
    import House from 'lucide-svelte/icons/house';
    import Tablet from 'lucide-svelte/icons/tablet';
    import Todo from 'lucide-svelte/icons/list-todo';
    import Library from 'lucide-svelte/icons/library-big';
    import Hash from 'lucide-svelte/icons/hash';
    import LogOut from 'lucide-svelte/icons/log-out';
    import UserRound from 'lucide-svelte/icons/user-round';
    import Settings from 'lucide-svelte/icons/settings';
    import ChevronsUpDownIcon from '@lucide/svelte/icons/chevrons-up-down';
    import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
    import type { User } from '$lib/types/ui-models';
    import { getContext } from 'svelte';

    let user = getContext<User>('user');
    const sidebar = useSidebar();

    function toLibrary(field: BoolField | null) {
        filterState.clearFilters();
        if (field) {
            filterState.boolFilters.set(field, true);
        }
        sidebar.setOpenMobile(false);
    }
</script>

<Sidebar.Root>
    <Sidebar.Header>
        <Sidebar.Menu>
            <Sidebar.MenuItem>
                <DropdownMenu.Root>
                    <DropdownMenu.Trigger>
                        {#snippet child({ props })}
                            <Sidebar.MenuButton
                                size="lg"
                                class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                                {...props}
                            >
                                <div
                                    class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg"
                                >
                                    <UserRound class="size-4" />
                                </div>
                                <div class="flex flex-col gap-0.5 leading-none">
                                    <span class="font-semibold">Hello, {user.name}</span>
                                </div>
                                <ChevronsUpDownIcon class="ms-auto" />
                            </Sidebar.MenuButton>
                        {/snippet}
                    </DropdownMenu.Trigger>
                    <DropdownMenu.Content class="w-(--bits-dropdown-menu-anchor-width)" align="start">
                        <DropdownMenu.Item onclick={() => sidebar.setOpenMobile(false)}>
                            {#snippet child({ props })}
                                <a href="/account" {...props}><Settings />Account Settings</a>
                            {/snippet}
                        </DropdownMenu.Item>
                        <DropdownMenu.Item onselect={() => sidebar.setOpenMobile(false)}>
                            {#snippet child({ props })}
                                <a href="/logout" {...props}><LogOut />Log Out</a>
                            {/snippet}
                        </DropdownMenu.Item>
                    </DropdownMenu.Content>
                </DropdownMenu.Root>
            </Sidebar.MenuItem>
        </Sidebar.Menu>
    </Sidebar.Header>
    <Sidebar.Content>
        <Sidebar.Group>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => sidebar.setOpenMobile(false)}>
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
                    <Sidebar.MenuItem>
                        <Sidebar.MenuButton onclick={() => sidebar.setOpenMobile(false)}>
                            {#snippet child({ props })}
                                <a href="/stats/fields" {...props}><Hash />By Fields</a>
                            {/snippet}
                        </Sidebar.MenuButton>
                    </Sidebar.MenuItem>
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
    </Sidebar.Content>
    <Sidebar.Footer />
</Sidebar.Root>
