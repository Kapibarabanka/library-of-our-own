<script lang="ts">
    import { type Fic, type UserImpression } from '$lib/types/domain-models';
    import { Button, buttonVariants } from '$ui/button/index.js';
    import * as Item from '$lib/components/ui/item/index.js';
    import * as Sheet from '$lib/components/ui/sheet';
    import { filterIcons, getImpressionIcon } from '$lib/utils/icon-utils';
    import { patchDetails, startedToday } from '$api/fics-details.remote';
    import { getKeyFromFic, getUserFicKey } from '$lib/utils/fic-utils';
    import FinishForm from '$lib/components/FinishForm.svelte';
    import * as Dialog from '$ui/dialog';
    import { Checkbox } from '$ui/checkbox';
    import { Label } from '$ui/label';
    import ImpressionInput from '$lib/components/ImpressionInput.svelte';
    import { formatDate } from '$lib/utils/label-utils';
    import KindleDialog from '$lib/components/KindleDialog.svelte';
    import { getContext } from 'svelte';
    import type { User } from '$lib/types/ui-models';
    import { updateAo3Info } from '$api/fics.remote';
    import { Spinner } from '$ui/spinner';
    import * as Card from '$ui/card';
    import { BookOpen, BookCheck, Info, type Icon as IconType } from '@lucide/svelte';
    import { BoolField, CustomField } from '@app/library/_types/filter-enums';
    import * as Popover from '$lib/components/ui/popover/index.js';

    let emailSet = !!getContext<User>('user').kindleEmail;

    let { fic, updateFic }: { fic: Fic; updateFic: () => Promise<void> } = $props();
    let key = getUserFicKey(fic);
    let spicy = $state(fic.details.spicy);
    let impression = $state<UserImpression | ''>(fic.details.impression ?? '');

    let formattedImpression = $derived(
        [
            ...(fic.details.impression
                ? [getImpressionIcon(fic.details.impression) + ' ' + fic.details.impression]
                : []),
            ...(fic.details.spicy ? ['🔥 Spicy'] : []),
        ].join(' and '),
    );

    let sheetOpened = $state(false);
    let rateDialogOpen = $state(false);
    let kindleDialogOpen = $state(false);

    async function startReading() {
        await startedToday(key);
        await updateFic();
    }

    function onFinishPressed() {
        sheetOpened = true;
    }

    async function onFinishSubmitted() {
        await updateFic();
        sheetOpened = false;
    }

    async function onRate() {
        await patchDetails({
            key,
            details: { ...fic.details, spicy: spicy, impression: !impression ? undefined : impression },
        });
        await updateFic();
        rateDialogOpen = false;
    }

    async function toggleBacklog() {
        await patchDetails({
            key,
            details: { ...fic.details, backlog: !fic.details.backlog },
        });
        await updateFic();
    }

    let loading = $state(false);

    async function updateInfo() {
        loading = true;
        const newInfo = await updateAo3Info(getKeyFromFic(fic));
        fic = { ...fic, ao3Info: newInfo };
        loading = false;
    }

    type ActionItem = {
        title: string | null;
        description: string | null;
        icon: typeof IconType;
        buttonText: string;
        primary?: boolean;
        buttonDisabled?: boolean;
        action: () => void;
    };

    let actionItems = $derived<ActionItem[]>([
        fic.readDatesInfo.canStart
            ? {
                  title: 'Not Reading Now',
                  description: null,
                  icon: BookOpen,
                  buttonText: 'Start Reaing',
                  primary: true,
                  action: () => startReading(),
              }
            : {
                  title: 'Reading since ' + formatDate(fic.readDatesInfo.readDates[0].startDate),
                  description: null,
                  icon: BookCheck,
                  buttonText: 'Finish',
                  primary: true,
                  action: () => onFinishPressed(),
              },

        {
            title: formattedImpression || 'Not Rated Yet',
            description: formattedImpression ? 'Your impression' : null,
            icon: filterIcons[CustomField.Impression],
            buttonText: 'Rate',
            action: () => {
                rateDialogOpen = true;
            },
        },
        {
            title: fic.details.backlog ? 'In backlog' : 'Not in backlog',
            description: null,
            icon: filterIcons[BoolField.Backlog],
            buttonText: fic.details.backlog ? 'Remove from backlog' : 'Add to backlog',
            action: () => toggleBacklog(),
        },
        {
            title: fic.details.isOnKindle ? 'On Kindle' : 'Not on Kindle',
            description: null,
            icon: filterIcons[BoolField.OnKindle],
            buttonText: fic.details.isOnKindle ? 'Mark as "Not on Kindle"' : 'Send to Kindle',
            action: () => {
                kindleDialogOpen = true;
            },
            buttonDisabled: !emailSet,
        },
    ]);
</script>

<Card.Root>
    <Card.Content class="py-2">
        <Item.Group>
            {#each actionItems as actionItem, index}
                <Item.Root variant="default" class="px-0 max-md:flex-col max-md:gap-2">
                    <Item.Content>
                        {#if actionItem.title}
                            <Item.Title>{actionItem.title}</Item.Title>
                        {/if}
                        {#if actionItem.description}
                            <Item.Description class="text-xs">{actionItem.description}</Item.Description>
                        {/if}
                    </Item.Content>
                    <Item.Actions>
                        <Button
                            class="w-[210px]"
                            variant={actionItem.primary ? 'default' : 'outline'}
                            onclick={actionItem.action}
                            disabled={actionItem.buttonDisabled}
                        >
                            <actionItem.icon />{actionItem.buttonText}
                        </Button>
                    </Item.Actions>
                </Item.Root>
                {#if index < actionItems.length}
                    <Item.Separator />
                {/if}
            {/each}

            <Item.Root class="px-0 max-md:flex-col max-md:gap-2">
                <Item.Content>
                    <Item.Description class="flex items-center">
                        Update fic in our database
                        <Popover.Root>
                            <Popover.Trigger class={buttonVariants({ variant: 'ghost', size: 'icon-sm' })}>
                                <Info />
                            </Popover.Trigger>
                            <Popover.Content class="w-80">
                                <div class="text-muted-foreground text-sm text-justify">
                                    Use this action if fic was updated on the Ao3 since parsing (e.g. tags were added or
                                    canonized, new chapters or works were posted).
                                </div>
                                <div class="text-muted-foreground text-sm text-justify">
                                    Please note, that for series it will only update the list of works and pull the tags
                                    from the new ones. If after sync tags of the series don't correspond with Ao3, try
                                    syncing each work separately.
                                </div>
                            </Popover.Content>
                        </Popover.Root>
                    </Item.Description>
                </Item.Content>
                <Item.Actions>
                    <Button variant="outline" class="w-[210px]" disabled={loading} onclick={() => updateInfo()}>
                        {#if loading}
                            <Spinner />
                        {/if}
                        Sync with Ao3
                    </Button>
                </Item.Actions>
            </Item.Root>
        </Item.Group>
    </Card.Content>
</Card.Root>

<Sheet.Root bind:open={sheetOpened}>
    <FinishForm {key} details={fic.details} onSubmitted={onFinishSubmitted}></FinishForm>
</Sheet.Root>

<Dialog.Root bind:open={rateDialogOpen}>
    <Dialog.Content class="sm:max-w-[425px]">
        <Dialog.Header>
            <Dialog.Title>Rate this fic</Dialog.Title>
        </Dialog.Header>
        <div class="flex items-center space-x-2">
            <Checkbox id="spicy" bind:checked={spicy} />
            <Label for="spicy">
                <span class="text-sm font-medium leading-none">🔥 Spicy</span>
            </Label>
        </div>
        <ImpressionInput bind:impression></ImpressionInput>
        <Dialog.Footer>
            <Button onclick={() => onRate()}>Save changes</Button>
        </Dialog.Footer>
    </Dialog.Content>
</Dialog.Root>

<KindleDialog
    bind:open={kindleDialogOpen}
    ficName={fic.ao3Info.title}
    ficKey={key}
    details={fic.details}
    onSubmitted={async () => await updateFic()}
></KindleDialog>

<style></style>
