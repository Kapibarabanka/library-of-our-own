<script lang="ts">
    import type { HomePageData } from '$lib/types/api-models';
    import { pageState } from './state.svelte';
    import * as Card from '$ui/card';
    import { Button } from '$ui/button';
    import { goto } from '$app/navigation';
    import Maximize from 'lucide-svelte/icons/maximize-2';
    import { Label } from '$ui/label';
    import FicCard from '@app/library/FicCard.svelte';
    import type { FicType } from '$lib/types/domain-models';
    import FicDetailsClient from '$api/FicDetailsClient';
    import FicsClient from '$api/FicsClient';
    import LoaderCircle from 'lucide-svelte/icons/loader-circle';
    import BookCheck from 'lucide-svelte/icons/book-check';
    import Trash2 from 'lucide-svelte/icons/trash-2';

    let { homePage }: { homePage: HomePageData } = $props();
    pageState.startedFics = homePage.currentlyReading;

    let isLoading = $state(false);

    async function finishFic(ficType: FicType, ficId: string, abandon: boolean) {
        isLoading = true;
        if (abandon) {
            await FicDetailsClient.abandonedToday(ficId, ficType);
        } else {
            await FicDetailsClient.finishedToday(ficId, ficType);
        }
        const newPage = await FicsClient.getHomePage();
        isLoading = false;
        pageState.startedFics = newPage.currentlyReading;
    }
</script>

<div class="flex flex-col gap-3 p-2">
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground">Currently Reading</Label>
        <div class="flex flex-col gap-2">
            {#each pageState.startedFics as startedFic}
                <Card.Root>
                    <Card.Header class="flex gap-2 flex-row p-3 pb-2">
                        <div class="flex-1">
                            <Card.Title>
                                <a href={startedFic.ao3Info.link}>{startedFic.ao3Info.title}</a>
                            </Card.Title>
                            <Card.Description>
                                <span class="text-xs">{startedFic.ao3Info.relationships?.join(', ')} </span>
                            </Card.Description>
                        </div>
                        <Button
                            variant="ghost"
                            class="p-1 h-4"
                            onclick={() => goto(`/fic/${startedFic.key.ficType.toLowerCase()}-${startedFic.key.ficId}`)}
                        >
                            <Maximize class="text-muted-foreground" size={15}></Maximize>
                        </Button>
                    </Card.Header>
                    <Card.Footer class="flex justify-between p-3 pt-0">
                        {#if isLoading}
                            <Button size="sm" disabled variant="outline"
                                ><LoaderCircle class="animate-spin" />Abandon</Button
                            >
                            <Button size="sm" disabled variant="outline"
                                ><LoaderCircle class="animate-spin" />Finish</Button
                            >
                        {:else}
                            <Button
                                size="sm"
                                variant="outline"
                                onclick={async () =>
                                    await finishFic(startedFic.key.ficType, startedFic.key.ficId, true)}
                            >
                                <Trash2 />Abandon
                            </Button>
                            <Button
                                size="sm"
                                variant="outline"
                                onclick={async () =>
                                    await finishFic(startedFic.key.ficType, startedFic.key.ficId, false)}
                                ><BookCheck />Finish</Button
                            >
                        {/if}
                    </Card.Footer>
                </Card.Root>
            {/each}
        </div>
    </div>
    <div class="flex flex-col">
        <Label class="text-center text-sm font-bold text-muted-foreground">Something you wanted to read</Label>
        {#if homePage.randomFicFromBacklog}
            <FicCard cardData={homePage.randomFicFromBacklog}></FicCard>
        {:else}
            <p>No fics in the reading list yet</p>
        {/if}
    </div>
</div>

<style></style>
