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

<Badge class="px-1 text-[15px] leading-4" variant="default" {onclick}>
    {getImpressionIcon(impression)}
    {#if withLabel}
        <span class="ml-1 text-[12px]">{shortImpression(impression)}</span>
    {/if}
</Badge>

<style></style>
