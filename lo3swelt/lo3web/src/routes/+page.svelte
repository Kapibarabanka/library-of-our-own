<script lang="ts">
	import FicCardsPage from '$lib/components/FicCardsPage/FicCardsPage.svelte';
	import type { PageProps } from './$types';
	let { data }: PageProps = $props();
</script>

{#await data.page}
	<p>Loading fics...</p>
{:then ficsPage}
	<FicCardsPage inputCards={ficsPage.cards}></FicCardsPage>
{:catch error}
	<p>Error {error.message}</p>
{/await}
