import type { ZonedDateTime } from '@internationalized/date';

export interface UserCookie {
    id: string;
    first_name: string | null;
    last_name: string | null;
    username: string | null;
    photo_url: string | null;
    auth_date: string | null;
    hash?: string;
}

export interface User {
    id: string;
    name: string;
    photoUrl: string | null;
    kindleEmail: string | undefined;
}

export enum StatUnit {
    Fics = 'fic',
    Words = 'word',
}

export interface DateRecord {
    id: string;
    dropped: boolean;
    startDate?: ZonedDateTime | undefined;
    finishDate?: ZonedDateTime | undefined;
    startOpened: boolean;
    finishOpened: boolean;
}
