package kapibarabanka.lo3.common
package models.ao3

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding.{encodeCharAs, percentEncode}
import scalaz.Scalaz.ToIdOps

import scala.util.matching.Regex

object Ao3Url {
  implicit val config: UriConfig = UriConfig(encoder =
    percentEncode -- '*' -- '.' -- '/' +
      encodeCharAs('+', "%2B")
  )
  private val baseUrl = Url(scheme = "https", host = "archiveofourown.org")
  private val tags    = baseUrl.addPathPart("tags")
  private val works   = baseUrl.addPathPart("works").addParam("view_adult", true)
  private val series  = baseUrl.addPathPart("series")

  private val seriesIdRegex: Regex = """^https://archiveofourown\.org:?/series/(\d+)(?:/.*)?$""".r
  private val workIdRegex: Regex   = """^https://archiveofourown\.org:?/works/(\d+)(?:[/?#].*)?$""".r
  private val htmlRegex: Regex     = """^https://archiveofourown\.org:?/downloads/(\d+)/(.*\.html)(?:\?updated_at=\d+)?$""".r
  private val mobiRegex: Regex     = """^https://archiveofourown\.org:?/downloads/(\d+)/(.*\.mobi)(?:\?updated_at=\d+)?$""".r

  def fic(id: String, ficType: FicType): String = ficType match
    case FicType.Work   => work(id)
    case FicType.Series => series(id)

  def work(id: String): String = works.addPathPart(id).toString

  def series(id: String): String = series.addPathPart(id).toString

  def seriesPage(id: String, page: Int): String = series.addPathPart(id).addParam(("page", page)).toString

  def tag(name: String): String = tags.addPathPart(name |> encodeForAo3).toString

  def download(link: String): String = baseUrl.addPathPart(link).toString

  def download(id: String, fileName: String): String =
    baseUrl.addPathPart("downloads").addPathPart(id).addPathPart(fileName).toString

  def cleanDownloadUrl(url: String) = url match {
    case htmlRegex(ficId, fileName) => Some(download(ficId, fileName))
    case mobiRegex(ficId, fileName) => Some(download(ficId, fileName))
    case _                          => None
  }

  def tryParseFicLink(url: String): Option[(String, FicType | String)] = url match
    case workIdRegex(id)            => Some((id, FicType.Work))
    case seriesIdRegex(id)          => Some((id, FicType.Series))
    case htmlRegex(ficId, fileName) => Some(ficId, fileName)
    case _                          => None

  private def encodeForAo3(path: String) = path.replace(".", "*d*").replace("/", "*s*")
}
