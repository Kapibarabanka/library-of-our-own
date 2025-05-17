<script lang="ts">
    import SimpleChart from '$lib/components/SimpleChart.svelte';
    import type { StatTagField, TagDataset } from '$lib/types/api-models';
    import * as Card from '$ui/card';
    import { type ChartConfiguration } from 'chart.js';

    let { months, datasets }: { months: string[]; datasets: TagDataset[] } = $props();

    let chartConfig: ChartConfiguration = $derived({
        type: 'bar',
        data: {
            labels: months.map(m => m.slice(0, 3)),
            datasets: datasets.map(ds => ({
                label: ds.tagValue,
                data: ds.counts,
                borderWidth: 1,
            })),
        },
        options: {
            scales: {
                x: {
                    stacked: true,
                },
                y: {
                    stacked: true,
                },
            },
        },
    });
</script>

<Card.Root>
    <Card.Content class="p-1">
        <SimpleChart {chartConfig}></SimpleChart>
    </Card.Content>
    <Card.Footer class="p-1 flex justify-around">
        <div>Totals</div>
    </Card.Footer>
</Card.Root>

<style></style>
