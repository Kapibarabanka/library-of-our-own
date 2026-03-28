<script lang="ts">
    import type { ReadDates, UserFicKey } from '$lib/types/domain-models';
    import * as Item from '$ui/item';
    import { Button, buttonVariants } from '$ui/button';
    import { Plus, Trash2, Info } from '@lucide/svelte';
    import Calendar from '$lib/components/ui/calendar/calendar.svelte';
    import * as Popover from '$lib/components/ui/popover/index.js';
    import { Label } from '$lib/components/ui/label/index.js';
    import ChevronDownIcon from '@lucide/svelte/icons/chevron-down';
    import {
        CalendarDate,
        fromDate,
        getLocalTimeZone,
        parseAbsoluteToLocal,
        toCalendarDate,
        today,
        ZonedDateTime,
        type DateValue,
    } from '@internationalized/date';
    import { Checkbox } from '$ui/checkbox';
    import type { DateRecord } from '$lib/types/ui-models';
    import moment from 'moment';
    import { setReadingHistory } from '$api/fics-details.remote';

    const timezone = getLocalTimeZone();
    const todayDate = today(timezone);

    let {
        key,
        originalDates,
        editFinished,
        cancel,
    }: { key: UserFicKey; originalDates: ReadDates[]; editFinished: () => void; cancel: () => void } = $props();

    let records = $state(
        originalDates?.map(
            d =>
                ({
                    id: crypto.randomUUID(),
                    dropped: d.isAbandoned,
                    startDate: parseAbsoluteToLocal(d.startDate + 'Z'),
                    finishDate: d.finishDate ? parseAbsoluteToLocal(d.finishDate + 'Z') : undefined,
                    startOpened: false,
                    finishOpened: false,
                }) as DateRecord,
        ) ?? [],
    );

    let hasErrors = $derived(records.some((r, idx) => getValidationError(r, idx) != null));

    let unavailialbleDates = $derived(
        records.map(r => {
            if (r.startDate == null || r.finishDate == null) {
                return [];
            }
            const dates: CalendarDate[] = [];
            let date = toCalendarDate(r.startDate!);
            const finish = toCalendarDate(r.finishDate!);
            while (date <= finish) {
                dates.push(date);
                date = date.add({ days: 1 });
            }
            return dates;
        }),
    );

    function getUnavailiableDates(index: number) {
        return unavailialbleDates.filter((_d, idx) => idx !== index).flat();
    }

    function getValidationError(record: DateRecord, index: number) {
        if (!record.startDate) {
            return 'Start date is required';
        }
        return index !== 0 && !record.finishDate ? 'Only the most recent record can be not finished' : null;
    }

    // start is restricted by unavailialbleDatesOnly, finish restricted by start from the bottom
    // and by next record from the top
    function getMaxFinishDate(record: DateRecord, index: number) {
        if (index > 0) {
            const nextRecord = records[index - 1];
            return nextRecord.startDate ?? todayDate;
        }
        return todayDate;
    }

    function sortDates() {
        records = [
            ...records.sort((a, b) => {
                if (a.startDate == null) {
                    return -1;
                }
                if (b.startDate == null) {
                    return 1;
                }
                return b.startDate.compare(a.startDate);
            }),
        ];
    }

    function getZoned(newValue: DateValue | undefined, startDate?: ZonedDateTime) {
        if (!newValue) {
            return undefined;
        }
        const now = fromDate(new Date(), timezone);
        let result = now.set({
            year: newValue.year,
            month: newValue.month,
            day: newValue.day,
        });
        if (startDate) {
            result = result.set({ hour: startDate.hour, minute: startDate.minute, second: startDate.second + 1 });
        }
        return result;
    }

    function changeStartDate(newValue: DateValue | undefined, record: DateRecord) {
        record.startDate = getZoned(newValue);
        record.startOpened = false;
        if (newValue == null || (record.finishDate != null && newValue > record.finishDate)) {
            record.finishDate = undefined;
            record.dropped = false;
        }
        sortDates();
    }

    function changeFinishDate(newValue: DateValue | undefined, record: DateRecord) {
        if (record.startDate) {
            record.finishDate = getZoned(newValue, record.startDate);
        }
        if (newValue == null) {
            record.dropped = false;
        }
        record.finishOpened = false;
        sortDates();
    }
    function addRecord() {
        const newRecord = {
            id: crypto.randomUUID(),
            dropped: false,
            startOpened: false,
            finishOpened: false,
        };
        if (records?.length) {
            records.splice(0, 0, newRecord);
        } else {
            records = [newRecord];
        }
    }

    function deleteRecord(index: number) {
        records.splice(index, 1);
    }

    async function submit() {
        await setReadingHistory({
            key,
            history: records
                .filter(r => r.startDate != null)
                .map(r => ({
                    startDate: r.startDate!.toAbsoluteString().slice(0, -1),
                    finishDate: r.finishDate?.toAbsoluteString().slice(0, -1),
                    isAbandoned: r.dropped,
                })),
        });
        editFinished();
    }
</script>

<div class="flex justify-between">
    <Button class="md:w-fit" variant="outline" onclick={addRecord}><Plus />Add record</Button>
    <div class="flex gap-4">
        <Button class="md:w-fit" variant="outline" onclick={cancel}>Cancel</Button>
        <Button class="md:w-fit" onclick={() => submit()} disabled={hasErrors}>Save</Button>
    </div>
</div>
<Item.Group>
    {#each records as record, index}
        <Item.Root>
            <Item.Content>
                {@const error = getValidationError(record, index)}
                {#if error}
                    <div class="text-red-700">{error}</div>
                {/if}
                <div class="flex flex-row flex-wrap gap-2 gap-y-3 justify-between">
                    <div class="flex flex-col gap-3">
                        <Label for="{record.id}-start" class="px-1">Start</Label>
                        <Popover.Root bind:open={record.startOpened}>
                            <Popover.Trigger id="{record.id}-start">
                                {#snippet child({ props })}
                                    <Button {...props} variant="outline" class="w-30 justify-between font-normal">
                                        {record.startDate
                                            ? moment(record.startDate.toDate()).format('YYYY-MM-DD')
                                            : 'Select'}
                                        <ChevronDownIcon />
                                    </Button>
                                {/snippet}
                            </Popover.Trigger>
                            <Popover.Content class="w-auto overflow-hidden p-0" align="start">
                                <Calendar
                                    type="single"
                                    bind:value={record.startDate}
                                    captionLayout="dropdown"
                                    onValueChange={val => changeStartDate(val, record)}
                                    maxValue={todayDate}
                                    isDateUnavailable={date =>
                                        getUnavailiableDates(index).some(
                                            d =>
                                                d.compare(toCalendarDate(date)) === 0 &&
                                                (record.startDate
                                                    ? d.compare(toCalendarDate(record.startDate)) !== 0
                                                    : true),
                                        )}
                                />
                            </Popover.Content>
                        </Popover.Root>
                    </div>
                    <div class="flex flex-col gap-3">
                        <Label for="{record.id}-finish" class="px-1">Finish</Label>
                        <Popover.Root bind:open={record.finishOpened}>
                            <Popover.Trigger id="{record.id}-finish">
                                {#snippet child({ props })}
                                    <Button
                                        {...props}
                                        variant="outline"
                                        class="w-30 justify-between font-normal"
                                        disabled={record.startDate == null}
                                    >
                                        {record.finishDate
                                            ? moment(record.finishDate.toDate()).format('YYYY-MM-DD')
                                            : 'Select'}
                                        <ChevronDownIcon />
                                    </Button>
                                {/snippet}
                            </Popover.Trigger>
                            <Popover.Content class="w-auto overflow-hidden p-0" align="start">
                                <Calendar
                                    type="single"
                                    bind:value={record.finishDate}
                                    captionLayout="dropdown"
                                    onValueChange={val => changeFinishDate(val, record)}
                                    maxValue={getMaxFinishDate(record, index)}
                                    minValue={record.startDate}
                                    placeholder={record.startDate}
                                />
                            </Popover.Content>
                        </Popover.Root>
                    </div>
                    <div class="flex items-center space-x-2">
                        <Checkbox
                            id="{record.id}-dropped"
                            bind:checked={record.dropped}
                            disabled={!record.finishDate}
                        />
                        <Label for="{record.id}-dropped" class="gap-0.5">
                            <span class="text-sm font-medium leading-none">Dropped</span>
                            <Popover.Root>
                                <Popover.Trigger class={buttonVariants({ variant: 'ghost', size: 'icon-sm' })}>
                                    <Info class="text-muted-foreground" />
                                </Popover.Trigger>
                                <Popover.Content class="w-80">
                                    <div class="text-muted-foreground text-sm text-justify">
                                        Dropped fics are not inluded in statistics calculation
                                    </div>
                                </Popover.Content>
                            </Popover.Root>
                        </Label>
                    </div>
                </div>
            </Item.Content>
            <Item.Actions>
                <Button variant="ghost" size="icon-sm" onclick={() => deleteRecord(index)}>
                    <Trash2 />
                </Button>
            </Item.Actions>
        </Item.Root>
        {#if index !== records.length - 1}
            <Item.Separator />
        {/if}
    {/each}
</Item.Group>

<style></style>
