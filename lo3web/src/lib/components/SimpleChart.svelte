<script lang="ts">
    import { Chart, registerables, type ChartConfiguration } from 'chart.js';

    let { chartConfig }: { chartConfig: ChartConfiguration } = $props();

    let chartObject: Chart;
    Chart.register(...registerables);

    function handleChart(node: HTMLCanvasElement, config: ChartConfiguration) {
        chartObject = new Chart(node, config);
        return {
            update(config: ChartConfiguration) {
                chartObject.destroy();
                chartObject = new Chart(node, config);
            },
            destroy() {
                chartObject.destroy();
            },
        };
    }
</script>

<canvas use:handleChart={chartConfig}></canvas>

<style></style>
