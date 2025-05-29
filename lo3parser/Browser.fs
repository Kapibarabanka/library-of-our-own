module Browser

open Models
open OpenQA.Selenium.Chrome
open OpenQA.Selenium
open System
open System.IO
open OpenQA.Selenium.Support.UI

// todo use env
let private downloadDir = "/Users/kapibarabanka/Documents/fics"
let private toError message = $"ERROR: {message}"

let downloadHtml (url: string) (fileName: string) =
    async {
        let options = new ChromeOptions()
        options.AddUserProfilePreference("download.default_directory", downloadDir)
        let driver = new ChromeDriver(options)
        driver.Navigate().GoToUrl url
        do! Async.Sleep 5000
        driver.Close()
        let filePath = $"{downloadDir}/{fileName}"
        let res = File.ReadAllText filePath
        File.Delete filePath
        let idx = res.IndexOf "<div id=\"chapters\""
        return res[.. idx - 1]
    }

let private getUnrestricted pageType driver =
    let wait = new WebDriverWait(driver, TimeSpan.FromMilliseconds 500)

    let elementToWait =
        driver.FindElement(
            match pageType with
            | Work -> By.ClassName "work"
            | Series -> By.CssSelector ".series.work"
            | Tag -> By.CssSelector ".tag.home.profile"
        )

    wait.Until(fun _ -> elementToWait.Displayed)
    driver.PageSource

let private tryGetWork driver =
    try
        getUnrestricted Work driver
    // todo match exception
    with ex ->
        let mes =
            if driver.PageSource.Contains "registred" then
                "restricted work"
            else
                ex.Message

        mes |> toError

let getPageSource (url: string) pageType =
    let driver = new ChromeDriver()

    try
        driver.Manage().Timeouts().ImplicitWait <- TimeSpan.FromMilliseconds(5000)

        try
            driver.Navigate().GoToUrl url

            match pageType with
            | Work -> tryGetWork driver
            | _ -> getUnrestricted pageType driver
        with ex ->
            let error = ex.Message |> toError
            error |> printfn "%s"
            error

    finally
        driver.Close()
