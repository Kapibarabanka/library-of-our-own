<script lang="ts">
    const { label, onclick }: { label: string; onclick?: () => void } = $props();
</script>

<li class="wrap">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    {#if onclick}
        <span class="text text-sm" {onclick}>{label}</span>
    {:else}
        <span class="text-sm">{label}</span>
    {/if}
</li>

<style>
    .wrap {
        display: inline;
        &:not(:last-child) {
            &::after {
                content: ',   ';
            }
        }
    }
    .text {
        border-bottom: 1px dotted grey;
        cursor: pointer;
    }
</style>
