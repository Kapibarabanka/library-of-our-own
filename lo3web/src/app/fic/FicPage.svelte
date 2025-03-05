<script lang="ts">
    import type { Fic } from '$lib/types/domain-models';

    let { fic }: { fic: Fic } = $props();
</script>

<div>Fic with title '{fic.ao3Info.title}'</div>

<style></style>
