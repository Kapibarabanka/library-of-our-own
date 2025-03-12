<script lang="ts">
    import { UserImpression } from '$lib/types/domain-models';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { shortImpression } from '$lib/utils/label-utils';
    import { Badge } from '$ui/badge';

    let {
        impression,
        withLabel = false,
        onclick = () => {},
    }: { impression: UserImpression; withLabel?: boolean; onclick?: () => void } = $props();
</script>

<Badge class={`justify-around px-1 text-[15px] leading-4 ${withLabel ? 'w-[77px]' : ''}`} variant="outline" {onclick}>
    <span class="flex gap-1">
        <span>{getImpressionIcon(impression)}</span>
        {#if withLabel}
            <span class="text-[12px]">{shortImpression(impression)}</span>
        {/if}
    </span>
</Badge>

<style></style>
