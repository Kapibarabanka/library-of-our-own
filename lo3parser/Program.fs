open Suave
open Suave.Filters
open System
open Suave.Operators
open Suave.Successful
open Suave.RequestErrors
open Suave.Utils.Collections
open Browser
open Models

let cfg =
    { defaultConfig with
        bindings = [ HttpBinding.createSimple HTTP "0.0.0.0" 9000 ]
        listenTimeout = TimeSpan.FromMilliseconds 3000. }


let tryGetPageSource q =
    match Option.ofChoice (q ^^ "url") with
    | None -> BAD_REQUEST "No url was provided"
    | Some url ->
        match Option.ofChoice (q ^^ "pageType") with
        | None -> BAD_REQUEST "No pageType was provided"
        | Some pageType -> OK(pageType |> toPageType |> getPageSource url)

let tryDownloadFile q =
    async {
        match Option.ofChoice (q ^^ "url") with
        | None -> return BAD_REQUEST "No url was provided"
        | Some url ->
            let! res = downloadFile url
            return OK("ok")
    }

let tryDownloadHtml q =
    async {
        match Option.ofChoice (q ^^ "url") with
        | None -> return BAD_REQUEST "No url was provided"
        | Some url ->
            match Option.ofChoice (q ^^ "fileName") with
            | None -> return BAD_REQUEST "No fileName was provided"
            | Some fileName ->
                let! res = downloadHtml url fileName
                return OK res
    }


let app =
    choose
        [ path "/hello" >=> fun (x: HttpContext) -> async { return! OK ("hello") x }
          path "/parser/source" >=> request (fun r -> tryGetPageSource r.query)
          path "/downloadHtml"
          >=> fun (x: HttpContext) ->
              async {
                  let! res = tryDownloadHtml x.request.query
                  return! res x
              }
          path "/downloadFile"
          >=> fun (x: HttpContext) ->
              async {
                  let! res = tryDownloadFile x.request.query
                  return! res x
              }
          NOT_FOUND "Found no handlers :c" ]

startWebServer cfg app
