open Suave
open Suave.Filters
open System
open Suave.Operators
open Suave.Successful
open Suave.RequestErrors
open OpenQA.Selenium.Chrome
open OpenQA.Selenium

let cfg =
  { defaultConfig with
      bindings = [ HttpBinding.createSimple HTTP "0.0.0.0" 9000 ]
      listenTimeout = TimeSpan.FromMilliseconds 3000. }

let echo str = "echoed " + str

let getPage() = 
  fun (x : HttpContext) ->
    async {
        let driver = new ChromeDriver()
        driver.Navigate().GoToUrl("https://archiveofourown.org/tags/Alternate%20Universe%20-%20Modern%20Setting")
        let source = driver.PageSource
        let header = driver.FindElement(By.Id("main"));
        let css = header.GetCssValue "padding-right"
        driver.Close()
        return! OK css x
    }

let app=
    choose [
    pathScan "/echo/%s" (fun s -> OK(echo s)) // dpesn'tt work in docker, i think pathscan is broken there
    path "/hello" >=> getPage()
    NOT_FOUND "Found no handlers :c"
    ]
startWebServer cfg app