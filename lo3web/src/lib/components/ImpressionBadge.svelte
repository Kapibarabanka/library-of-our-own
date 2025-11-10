<script lang="ts">
    import { UserImpression } from '$lib/types/domain-models';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { shortImpression } from '$lib/utils/label-utils';
    import IconBadge from './IconBadge.svelte';

    let {
        impression,
        withLabel = false,
        onclick = () => {},
    }: { impression: UserImpression; withLabel?: boolean; onclick?: () => void } = $props();
</script>

<IconBadge icon={getImpressionIcon(impression)} text={withLabel ? shortImpression(impression) : undefined} {onclick}
></IconBadge>

<style></style>
