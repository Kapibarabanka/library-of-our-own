import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import { useState } from 'react';

export default function BoolFilterComponent({
    defaultValue,
    onOptionSelected,
}: {
    defaultValue: boolean | undefined;
    onOptionSelected: (selection: boolean) => void;
}) {
    const [value, setValue] = useState<string | undefined>(defaultValue?.toString() ?? undefined);
    function handleChange(val: string) {
        onOptionSelected(val === 'true');
        setValue(val);
    }
    return (
        <div>
            <RadioGroup row value={value} onChange={e => handleChange(e.target.value)}>
                <FormControlLabel value='true' control={<Radio />} label='Yes' />
                <FormControlLabel value='false' control={<Radio />} label='No' />
            </RadioGroup>
        </div>
    );
}
