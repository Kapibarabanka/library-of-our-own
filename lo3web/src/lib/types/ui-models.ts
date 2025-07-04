export interface UserCookie {
    id: string;
    first_name: string | null;
    last_name: string | null;
    username: string | null;
    photo_url: string | null;
    auth_date: string | null;
    hash: string;
}

export interface User {
    id: string;
    name: string;
    photoUrl: string | null;
}
