<script lang="ts">
    import { type ChartConfiguration } from 'chart.js/auto';
    import * as Card from '$ui/card';
    import type { MonthStats } from '$lib/types/api-models';
    import SimpleChart from '$lib/components/SimpleChart.svelte';

    let { stats }: { stats: MonthStats[] } = $props();
    let totalFics = $derived(stats.map(s => s.fics).reduce((prev, curr) => prev + curr, 0));
    let totalWords = $derived(stats.map(s => s.words).reduce((prev, curr) => prev + curr, 0));

    let chartConfig: ChartConfiguration = $derived({
        type: 'line',
        data: {
            labels: stats.map(s => s.month.slice(0, 3)),
            datasets: [
                {
                    label: '# of fics',
                    data: stats.map(s => s.fics),
                    borderWidth: 1,
                    yAxisID: 'y',
                },
                {
                    label: 'K Words',
                    data: stats.map(s => s.words),
                    borderWidth: 1,
                    yAxisID: 'y1',
                },
            ],
        },
        options: {
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false, // only want the grid lines for one axis to show up
                    },
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
        <div>Total fics: {totalFics}</div>
        <div>Total words: {totalWords.toLocaleString('en-us')}K</div>
    </Card.Footer>
</Card.Root>

<style></style>
