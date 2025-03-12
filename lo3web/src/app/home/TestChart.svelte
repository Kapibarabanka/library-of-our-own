<script lang="ts">
    import { Chart, registerables } from 'chart.js';
    import { onMount } from 'svelte';
    import * as Card from '$ui/card';

    Chart.register(...registerables);
    onMount(() => {
        console.log('the component has mounted');
        const ctx = document.getElementById('myChart');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [
                    {
                        label: '# of fics',
                        data: [12, 19, 3, 5, 2, 3],
                        borderWidth: 1,
                        yAxisID: 'y',
                    },
                    {
                        label: 'K Words',
                        data: [10.0, 90.567, 11.23, 36.905, 90.099, 443.543],
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

                        // grid line settings
                        grid: {
                            drawOnChartArea: false, // only want the grid lines for one axis to show up
                        },
                    },
                },
            },
        });
    });
</script>

<Card.Root>
    <Card.Content class="p-1">
        <canvas id="myChart"></canvas>
    </Card.Content>
</Card.Root>

<style></style>
