<script lang="ts">
    import HomePage from '@app/home/HomePage.svelte';
    import type { PageProps } from './$types';

    let { data }: PageProps = $props();
</script>

{#await data.homePage}
    <p>Loading home page...</p>
{:then homePage}
    <HomePage {homePage}></HomePage>
{:catch error}
    <p>Error {error.message}</p>
{/await}
