<script lang="ts">
    import * as Card from '$ui/card';
    import type { MonthStats } from '$lib/types/api-models';
    import * as Chart from '$lib/components/ui/chart/index.js';
    import { BarChart } from 'layerchart';
    import { cubicInOut } from 'svelte/easing';

    let { stats }: { stats: MonthStats[] } = $props();

    const chartConfig = {
        words: {
            label: 'Words',
            color: '#2563eb',
        },
        fics: {
            label: 'Fics',
            color: '#60a5fa',
        },
    } satisfies Chart.ChartConfig;

    let activeChart = $state<keyof typeof chartConfig>('words');
    const activeSeries = $derived([
        {
            key: activeChart,
            label: chartConfig[activeChart].label,
            color: chartConfig[activeChart].color,
        },
    ]);
    const total = $derived({
        words: stats?.reduce((acc, curr) => acc + curr.words, 0) ?? 0,
        fics: stats?.reduce((acc, curr) => acc + curr.fics, 0) ?? 0,
    });
</script>

<Card.Root>
    <Card.Header class="flex flex-col items-stretch space-y-0 border-b p-0 sm:flex-row">
        <div class="flex flex-1 flex-col justify-center gap-1 px-6 py-5 sm:py-6">
            <Card.Title>Reading stats for last 6 months</Card.Title>
            <Card.Description>Click on a total block to switch units</Card.Description>
        </div>
        <div class="flex">
            {#each ['words', 'fics'] as key (key)}
                {@const chart = key as keyof typeof chartConfig}
                <button
                    data-active={activeChart === chart}
                    class="data-[active=true]:bg-muted/50 relative z-30 flex flex-1 flex-col justify-center gap-1 border-t px-6 py-4 text-left even:border-l sm:border-l sm:border-t-0 sm:px-8 sm:py-6"
                    onclick={() => (activeChart = chart)}
                >
                    <span class="text-muted-foreground text-xs">
                        Total {chartConfig[chart].label}
                    </span>
                    <span class="text-lg font-bold leading-none sm:text-3xl">
                        {total[key as keyof typeof total].toLocaleString()}
                    </span>
                </button>
            {/each}
        </div>
    </Card.Header>
    <Card.Content class="p-1">
        <Chart.Container config={chartConfig} class="min-h-[200px] w-full">
            <BarChart
                data={stats}
                tooltip={false}
                labels={{ placement: 'outside' }}
                orientation="horizontal"
                axis="y"
                y="month"
                seriesLayout="group"
                padding={{ left: 40, right: 40 }}
                series={activeSeries}
                props={{
                    bars: {
                        stroke: 'none',
                        radius: 5,
                        rounded: 'all',
                        initialWidth: 0,
                        initialX: 0,
                        motion: {
                            x: { type: 'tween', duration: 500, easing: cubicInOut },
                            width: { type: 'tween', duration: 500, easing: cubicInOut },
                        },
                    },
                    yAxis: { format: d => d.slice(0, 3) },
                }}
            ></BarChart>
        </Chart.Container>
    </Card.Content>
</Card.Root>

<style></style>
