<script lang="ts">
    const { label, withCross = false, onclick }: { label: string; withCross?: boolean; onclick: () => void } = $props();
</script>

<li class="wrap">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="text" {onclick}>
        {label + (withCross ? ' X' : '')}
    </span>
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
