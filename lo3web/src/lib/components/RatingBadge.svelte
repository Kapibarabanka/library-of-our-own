<script lang="ts">
    import { Rating } from '$lib/types/domain-models';
    import { getRatingBackground } from '$lib/utils/icon-utils';
    import { shortRating } from '$lib/utils/label-utils';
    import { Badge } from '$ui/badge';

    let {
        rating,
        variant = 'letter',
        onclick = () => {},
    }: { rating: Rating; variant?: 'letter' | 'short' | 'full'; onclick?: () => void } = $props();
    const color = $derived(getRatingBackground(rating));
</script>

<Badge class={`px-1.5 ${color}`} variant="outline" {onclick}>
    {#if variant === 'letter'}
        {rating[0]}
    {:else if variant === 'short'}
        {shortRating(rating)}
    {:else}
        {rating}
    {/if}
</Badge>

<style></style>
