import { StatTagField } from '$lib/types/api-models';
import { filterIcons } from '$lib/utils/icon-utils';
import { TagField, tagFieldLabels } from '@app/library/_types/filter-enums';

export const statToFields: Record<StatTagField, TagField> = {
    [StatTagField.Ship]: TagField.relationships,
    [StatTagField.Fandom]: TagField.fandoms,
    [StatTagField.Freeform]: TagField.tags,
};

export const statFieldsItems = Object.fromEntries(
    Object.values(StatTagField).map(statField => {
        const tagField = statToFields[statField];
        return [statField as StatTagField, { label: tagFieldLabels[tagField], icon: filterIcons[tagField] }];
    }),
);
