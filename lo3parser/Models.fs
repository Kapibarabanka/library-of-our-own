module Models

type PageType =
    | Work
    | Series
    | Tag

let toPageType str =
    match str with
    | "work" -> Work
    | "series" -> Series
    | _ -> Tag
