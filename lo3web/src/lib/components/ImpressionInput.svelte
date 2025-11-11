<script lang="ts">
    import { UserImpression } from '$lib/types/domain-models';
    import { getImpressionIcon } from '$lib/utils/icon-utils';
    import { shortImpression } from '$lib/utils/label-utils';
    import { Label } from '$ui/label';
    import * as Tabs from '$ui/tabs';

    let { impression = $bindable() }: { impression: UserImpression | '' } = $props();
</script>

<div class="flex flex-col gap-1">
    <Label for="impression" class="text-center">
        <span class="text-sm leading-none">Your impression</span>
    </Label>
    <Tabs.Root id="impression" bind:value={impression} class="max-w-[500px]">
        <Tabs.List class="grid w-full grid-cols-5">
            {#each Object.values(UserImpression) as impr}
                <Tabs.Trigger value={impr} class="flex gap-1">
                    {getImpressionIcon(impr)}
                    {#if impr === impression}
                        <span class="text-[12px]">{shortImpression(impr)}</span>
                    {/if}
                </Tabs.Trigger>
            {/each}
        </Tabs.List>
    </Tabs.Root>
</div>

<style></style>
