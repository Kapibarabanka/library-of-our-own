<script lang="ts">
    import type { PageProps } from './$types';

    let { data }: PageProps = $props();
</script>

<div>FicPage</div>
<div>{data.ficType} with id {data.id}</div>

<style></style>
