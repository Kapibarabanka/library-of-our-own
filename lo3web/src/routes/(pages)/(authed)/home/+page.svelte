<script lang="ts">
    import HomePage from '@app/home/HomePage.svelte';
    import { getHomePage } from '$api/fics.remote';
</script>

<svelte:boundary>
    <HomePage homePage={await getHomePage()}></HomePage>
    {#snippet pending()}
        <p>Loading home page...</p>
    {/snippet}
    {#snippet failed(error)}
        <p>Error: {error}</p>
    {/snippet}
</svelte:boundary>
