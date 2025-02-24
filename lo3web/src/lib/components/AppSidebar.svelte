<script lang="ts">
    import * as Sidebar from '$lib/components/ui/sidebar/index.js';
    import { useSidebar } from '$lib/components/ui/sidebar/index.js';
    const sidebar = useSidebar();
    const links: { title: string; href: string }[] = [
        {
            title: 'Home',
            href: '/',
        },
        {
            title: 'All Fics',
            href: '/library',
        },
        {
            title: 'Reading List',
            href: '/reading-list',
        },
        {
            title: 'Fics on Kindle',
            href: '/on-kindle',
        },
    ];
</script>

<Sidebar.Root>
    <Sidebar.Header>Hello Kapibarabanka</Sidebar.Header>
    <Sidebar.Content>
        <Sidebar.Group>
            <Sidebar.GroupContent>
                <Sidebar.Menu>
                    {#each links as link (link.title)}
                        <Sidebar.MenuItem>
                            <Sidebar.MenuButton onclick={() => sidebar.toggle()}>
                                {#snippet child({ props })}
                                    <a href={link.href} {...props}>{link.title}</a>
                                {/snippet}
                            </Sidebar.MenuButton>
                        </Sidebar.MenuItem>
                    {/each}
                </Sidebar.Menu>
            </Sidebar.GroupContent>
        </Sidebar.Group>
    </Sidebar.Content>
    <Sidebar.Footer />
</Sidebar.Root>
