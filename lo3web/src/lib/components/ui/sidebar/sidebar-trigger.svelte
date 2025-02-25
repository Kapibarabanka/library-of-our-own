<script lang="ts">
    import { Button } from '$lib/components/ui/button/index.js';
    import { cn } from '$lib/components/ui/utils.js';
    import Menu from 'lucide-svelte/icons/menu';
    import type { ComponentProps } from 'svelte';
    import { useSidebar } from './context.svelte.js';

    let {
        ref = $bindable(null),
        class: className,
        onclick,
        ...restProps
    }: ComponentProps<typeof Button> & {
        onclick?: (e: MouseEvent) => void;
    } = $props();

    const sidebar = useSidebar();
</script>

<Button
    type="button"
    onclick={e => {
        onclick?.(e);
        sidebar.toggle();
    }}
    data-sidebar="trigger"
    variant="ghost"
    class={cn('h-10 w-10 [&_svg]:size-6', className)}
    {...restProps}
>
    <Menu />
    <span class="sr-only">Toggle Sidebar</span>
</Button>
