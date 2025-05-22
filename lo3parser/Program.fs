open Suave
open Suave.Filters
open System
open Suave.Operators
open Suave.Successful
open Suave.RequestErrors
open System.Text.Json
open Suave.Writers
open Suave.Utils.Collections
open Models
open Parser

let cfg =
    { defaultConfig with
        bindings = [ HttpBinding.createSimple HTTP "0.0.0.0" 9000 ]
        listenTimeout = TimeSpan.FromMilliseconds 3000. }

let echo str = "echoed " + str

let getPage () =
    fun (x: HttpContext) -> async { return! OK (getTestValue ()) x }

type Lo3Error = { msg: string }

let tryParseWork q =
    let maybeId = Option.ofChoice (q ^^ "id")

    match maybeId with
    | None -> BAD_REQUEST "No id was provided"
    | Some id -> OK(id |> parseWork |> JsonSerializer.Serialize)

let tryGetPageSource q =
    match Option.ofChoice (q ^^ "url") with
    | None -> BAD_REQUEST "No url was provided"
    | Some url ->
        match Option.ofChoice (q ^^ "pageType") with
        | None -> BAD_REQUEST "No pageType was provided"
        | Some pageType -> OK(getPageSource url pageType)


let app =
    choose
        [ path "/hello"
          >=> fun (x: HttpContext) -> async { return! OK (getTestValue ()) x }
          path "/parser/work"
          >=> request (fun r -> tryParseWork r.query)
          >=> setMimeType "application/json; charset=utf-8"
          path "/parser/source" >=> request (fun r -> tryGetPageSource r.query)
          NOT_FOUND "Found no handlers :c" ]

startWebServer cfg app
