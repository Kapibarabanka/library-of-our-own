<script lang="ts">
    import { getTagFieldStats } from '$api/stats.remote';
    import { StatTagField } from '$lib/types/api-models';
    import * as Tabs from '$ui/tabs';
    import * as Select from '$ui/select';
    import * as Card from '$ui/card';
    import { BarChart, Highlight } from 'layerchart';
    import { cubicInOut } from 'svelte/easing';
    import * as Chart from '$lib/components/ui/chart';
    import { StatUnit } from '$lib/types/ui-models';

    const chartRedrawDealy = 3;
    const colors = [
        '#DB8098',
        '#EEAABF',
        '#FFB199',
        '#B3DE9F',
        '#57A4B2',
        '#4092E9',
        '#859DFF',
        '#48699C',
        '#8555B1',
        '#FDFDC4',
    ];
    const initialTag = StatTagField.Ship;
    let stats = $state(await getTagFieldStats(initialTag));

    let field: StatTagField = $state(initialTag);
    let unit: StatUnit = $state(StatUnit.Words);
    let chartConfig: Chart.ChartConfig = $derived(
        stats.allLabels
            .map((l, idx) => ({ label: l, color: colors[idx] }))
            .reduce((a, { label, color }) => ({ ...a, [label]: { label, color } }), {})
    );
    let currentTop = $derived(unit === StatUnit.Words ? stats.topByWords : stats.topByFics);
    let series = $derived(
        currentTop.map(({ label }) => ({
            key: label,
            label: label,
            color: chartConfig[label].color,
        }))
    );

    let legend = $derived(
        currentTop.map(({ label, value }) => ({
            label,
            total: value,
            color: chartConfig[label].color,
        }))
    );

    let data = $derived(
        stats.datasets.map(ds =>
            (unit === StatUnit.Words ? ds.byWords : ds.byFics).reduce(
                (a, { label, value }) => ({ ...a, [label]: value }),
                {
                    timeLabel: ds.timeLabel,
                }
            )
        )
    );
    let showChart = $state(true);
</script>

<div class="flex flex-col gap-4 p-4">
    <Card.Root>
        <Card.Header class="px-4">
            <div class="flex flex-wrap justify-between gap-2">
                <div>
                    <Card.Title>Top for last 6 months</Card.Title>
                    <Card.Description>You can choose field and unit</Card.Description>
                </div>
                <div class="flex gap-1">
                    <Select.Root
                        type="single"
                        value={field}
                        onValueChange={async val => {
                            showChart = false;
                            field = val as StatTagField;
                            stats = await getTagFieldStats(val as StatTagField);
                            showChart = true;
                        }}
                    >
                        <Select.Trigger class="w-[110px]">
                            {field}
                        </Select.Trigger>
                        <Select.Content>
                            <Select.Group>
                                {#each Object.values(StatTagField) as tagField}
                                    <Select.Item value={tagField} label={tagField}></Select.Item>
                                {/each}
                            </Select.Group>
                        </Select.Content>
                    </Select.Root>

                    <Tabs.Root
                        value={unit}
                        onValueChange={async val => {
                            showChart = false;
                            unit = val as StatUnit;
                            await new Promise(f => setTimeout(f, chartRedrawDealy));
                            showChart = true;
                        }}
                    >
                        <Tabs.List class="grid w-full grid-cols-2">
                            {#each Object.values(StatUnit) as un}
                                <Tabs.Trigger value={un} class="flex gap-1">By {un}s</Tabs.Trigger>
                            {/each}
                        </Tabs.List>
                    </Tabs.Root>
                </div>
            </div>
        </Card.Header>
        <Card.Content class="p-1">
            <div class="aspect-video">
                {#if showChart}
                    <Chart.Container config={chartConfig}>
                        <BarChart
                            {data}
                            orientation="horizontal"
                            axis={true}
                            y="timeLabel"
                            seriesLayout="stack"
                            {series}
                            padding={{ top: 10, left: 40, right: 40, bottom: 30 }}
                            props={{
                                bars: {
                                    stroke: 'none',
                                    radius: 5,
                                    rounded: 'none',
                                    initialWidth: 0,
                                    initialX: 0,
                                    motion: {
                                        x: { type: 'tween', duration: 500, easing: cubicInOut },
                                        width: { type: 'tween', duration: 500, easing: cubicInOut },
                                    },
                                },
                                highlight: { area: false },
                                yAxis: { format: d => d.slice(0, 3) },
                            }}
                        >
                            {#snippet tooltip()}
                                <Chart.Tooltip />
                            {/snippet}
                            {#snippet belowMarks()}
                                <Highlight area={{ class: 'fill-muted' }} />
                            {/snippet}
                        </BarChart>
                    </Chart.Container>
                {/if}
            </div>
        </Card.Content>
        <Card.Footer class="flex flex-wrap gap-x-3 gap-y-2 justify-around">
            {#each legend as { label, color, total }}
                <div class="flex gap-1">
                    <div class="h-4 w-4 rounded-full" style:background-color={color}></div>
                    <span class="text-xs">{label} ({total.toLocaleString('en-us')} {unit}s)</span>
                </div>
            {/each}
        </Card.Footer>
    </Card.Root>
</div>

<style></style>
