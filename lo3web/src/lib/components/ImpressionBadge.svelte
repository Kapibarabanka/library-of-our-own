<script lang="ts">
    import { UserImpression } from '$lib/types/domain-models';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { Badge } from '$ui/badge';

    let {
        impression,
        withLabel = false,
        onclick = () => {},
    }: { impression: UserImpression; withLabel?: boolean; onclick?: () => void } = $props();
</script>

<Badge class="px-1 text-[15px] leading-4" variant="outline" {onclick}>
    {getImpressionIcon(impression)}
    {#if withLabel}
        <span class="ml-1 text-[12px]">{impression}</span>
    {/if}
</Badge>

<style></style>
