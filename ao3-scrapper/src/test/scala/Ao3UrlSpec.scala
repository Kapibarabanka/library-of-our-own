import com.kapibarabanka.ao3scrapper.utils.Ao3Url
import org.scalatest.funsuite.AnyFunSuite

class Ao3UrlSpec extends AnyFunSuite {
  test("work url") {
    val actual   = Ao3Url.work("12345")
    val expected = "https://archiveofourown.org/works/12345?view_adult=true"
    assert(actual == expected)
  }

  test("Dr. Ratio (Honkai: Star Rail)") {
    val actual   = Ao3Url.tag("Dr. Ratio (Honkai: Star Rail)")
    val expected = "https://archiveofourown.org/tags/Dr*d*%20Ratio%20%28Honkai%3A%20Star%20Rail%29"
    assert(actual == expected)
  }

  test("Zoro/Sanji") {
    val actual   = Ao3Url.tag("Zoro/Sanji")
    val expected = "https://archiveofourown.org/tags/Zoro*s*Sanji"
    assert(actual == expected)
  }

  test("Zoro & Sanji") {
    val actual   = Ao3Url.tag("Zoro & Sanji")
    val expected = "https://archiveofourown.org/tags/Zoro%20%26%20Sanji"
    assert(actual == expected)
  }

  test("5+1 Things") {
    val actual   = Ao3Url.tag("5+1 Things")
    val expected = "https://archiveofourown.org/tags/5%2B1%20Things"
    assert(actual == expected)
  }

}
