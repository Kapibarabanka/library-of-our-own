<script lang="ts">
    import ImpressionBadge from '$lib/components/ImpressionBadge.svelte';
    import RatingIcon from '$lib/components/RatingBadge.svelte';
    import { FicType, type Ao3FicInfo, type FicDetails } from '$lib/types/domain-models';
    import Badge from '$ui/badge/badge.svelte';
    import { BoolField } from './_types/filter-enums';
    import { AppliedFiltersState, filterState as globalFilterState } from './state.svelte';
    import Tablet from 'lucide-svelte/icons/tablet';
    import IconBadge from '$lib/components/IconBadge.svelte';

    let { ao3Info, details, canFilter }: { ao3Info: Ao3FicInfo; details: FicDetails; canFilter: boolean } = $props();

    let partType = $derived(ao3Info.ficType === FicType.Work ? 'chapter' : 'work');
    let multiple = $derived(ao3Info.partsWritten === 1 ? '' : 's');
    let completed = $derived(ao3Info.complete ? 'Completed' : 'Not completed');
    let formattedWords = $derived(ao3Info.words.toLocaleString('en-us'));

    function filterIfPossible(filterCallback: (filter: AppliedFiltersState) => void) {
        if (canFilter) {
            filterCallback(globalFilterState);
        }
    }
</script>

<div class="w-full flex flex-col-reverse md:flex-row md:justify-between gap-2">
    <div class="flex gap-2">
        <RatingIcon
            rating={ao3Info.rating}
            onclick={() => filterIfPossible(f => f.allowedRatings.add(ao3Info.rating))}
        />
        {#if details.isOnKindle}
            <Badge variant="outline" onclick={() => filterIfPossible(f => f.boolFilters.set(BoolField.OnKindle, true))}>
                <Tablet size={15}></Tablet>
            </Badge>
        {/if}
        {#if details.impression}
            <ImpressionBadge
                impression={details.impression}
                onclick={() => filterIfPossible(f => f.allowedImpressions.add(details.impression!))}
            ></ImpressionBadge>
        {/if}
        {#if details.spicy}
            <IconBadge icon="🔥" onclick={() => filterIfPossible(f => f.boolFilters.set(BoolField.Spicy, true))}
            ></IconBadge>
        {/if}
    </div>
    <div class="flex gap-2">
        <Badge variant="outline">{completed}</Badge>
        <Badge variant="outline">
            <span>{ao3Info.partsWritten} {partType}{multiple}</span>
        </Badge>
        <Badge variant="outline">
            {formattedWords} words
        </Badge>
    </div>
</div>

<style></style>
