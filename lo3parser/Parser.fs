module Parser

open Models
open OpenQA.Selenium.Chrome
open OpenQA.Selenium
open System
open OpenQA.Selenium.Support.UI

let getTestValue () =
    let driver = new ChromeDriver()
    driver.Navigate().GoToUrl("https://archiveofourown.org/tags/Alternate%20Universe%20-%20Modern%20Setting")
    let header = driver.FindElement(By.Id "main")
    let css = header.GetCssValue "padding-right"
    driver.Close()
    css

let useDriverForAo3 (url: string) callback =
    let driver = new ChromeDriver()

    try
        driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromMilliseconds(1000)

        driver.Navigate().GoToUrl url

        callback driver
    finally
        driver.Close()


let parseWork id =
    useDriverForAo3 $"https://archiveofourown.org/works/{id}?view_adult=true" (fun driver ->
        let rating = driver.FindElement(By.CssSelector ".rating.tags")
        let title = rating.Text

        let work = { id = id; title = title; authors = [] }
        work)

let getPageSource (url: string) pageType =
    let driver = new ChromeDriver()

    try
        driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromMilliseconds(500)

        try
            driver.Navigate().GoToUrl url
            let wait = new WebDriverWait(driver, TimeSpan.FromMilliseconds(2000))

            let res =
                match pageType with
                | "work" -> wait.Until(fun d -> (driver.FindElement(By.ClassName "work")).Displayed)
                | "series" -> wait.Until(fun d -> (driver.FindElement(By.CssSelector ".series.work")).Displayed)
                | _ -> wait.Until(fun d -> (driver.FindElement(By.CssSelector ".tag.home.profile")).Displayed)

            driver.PageSource
        with ex ->
            let error = $"ERROR: {ex.ToString()}"
            error |> printfn "%s"
            error

    finally
        driver.Close()
