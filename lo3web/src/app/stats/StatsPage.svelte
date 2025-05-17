<script lang="ts">
    import type { StatTagField, TagFieldStats } from '$lib/types/api-models';
    import { Label } from '$ui/label';
    import TagStatsChart from './TagStatsChart.svelte';

    let { tagField, stats }: { tagField: StatTagField; stats: TagFieldStats } = $props();
</script>

<div class="flex flex-col gap-2">
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground"
            >Top {tagField.toLowerCase()}s by fic count</Label
        >
        <TagStatsChart months={stats.months} datasets={stats.byFics}></TagStatsChart>
    </div>
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground"
            >Top {tagField.toLowerCase()}s by word count (in K words)</Label
        >
        <TagStatsChart months={stats.months} datasets={stats.byWords}></TagStatsChart>
    </div>
</div>

<style></style>
