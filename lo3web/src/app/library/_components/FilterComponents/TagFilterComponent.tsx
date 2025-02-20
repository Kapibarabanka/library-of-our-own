import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import { useState } from 'react';
import { TagInclusion } from '@/app/library/_types/filter-enums';
import { TagFilterItem } from '@/app/library/_types/FiltersData';
import { MenuItem } from '@mui/material';

export default function TagFilterComponent({
    filteredField,
    filterItems,
    onTagSelected,
}: {
    filteredField: string;
    filterItems: TagFilterItem[];
    onTagSelected: (tagInclusion: TagInclusion, tag?: string) => void;
}) {
    const [tagInclusion, setTagInclusion] = useState(TagInclusion.Include);
    const [autocompleteInput, setAutocompleteInput] = useState('');
    const [autocompleteValue, setAutocompleteValue] = useState<TagFilterItem | null>(null);
    return (
        <div className='flex flex-row gap-1'>
            <TextField
                size='small'
                select
                value={tagInclusion}
                onChange={e => setTagInclusion(e.target.value as TagInclusion)}>
                {Object.values(TagInclusion).map(filterType => (
                    <MenuItem key={filterType} value={filterType}>
                        {filterType}
                    </MenuItem>
                ))}
            </TextField>
            <Autocomplete
                disablePortal
                className='flex-1'
                value={autocompleteValue}
                inputValue={autocompleteInput}
                clearOnBlur={true}
                onInputChange={(e, val) => setAutocompleteInput(val)}
                onClose={() => {
                    setAutocompleteInput('');
                    setAutocompleteValue(null);
                }}
                options={filterItems}
                onChange={($e, selectedItem) => onTagSelected(tagInclusion, selectedItem?.value)}
                renderInput={params => (
                    <TextField {...params} label={`Select ${filteredField.toLocaleLowerCase()}`} size='small' />
                )}
            />
        </div>
    );
}
