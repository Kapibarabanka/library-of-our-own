<script lang="ts">
    import { Badge } from '$ui/badge';

    let {
        icon,
        text = undefined,
        onclick = () => {},
    }: { icon: string; text?: string | undefined; onclick?: () => void } = $props();
    let withText = !!text;
</script>

<Badge class={`justify-around px-1 text-[15px] leading-4 ${withText ? 'w-[77px]' : ''}`} variant="outline" {onclick}>
    <span class="flex gap-1">
        <span>{icon}</span>
        {#if withText}
            <span class="text-[12px]">{text}</span>
        {/if}
    </span>
</Badge>

<style></style>
