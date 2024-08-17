package com.kapibarabanka.ao3scrapper

import com.kapibarabanka.ao3scrapper.models.FicType
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding.{encodeCharAs, percentEncode}

import scala.util.matching.Regex

object Ao3Url {
  implicit val config: UriConfig = UriConfig(encoder =
    percentEncode -- '/' +
      encodeCharAs('+', "%2B") +
      encodeCharAs('.', "*d*") +
      encodeCharAs('/', "*s*")
  )
  val base = "https://archiveofourown.org"
  private val baseUrl   = Url(base)
  private val tags   = baseUrl.addPathPart("tags")
  private val works  = baseUrl.addPathPart("works").addParam("view_adult", true)
  private val series = baseUrl.addPathPart("series")

  private val seriesIdRegex: Regex = """^https://archiveofourown\.org:?/series/(\d+)(?:/.*)?$""".r
  private val workIdRegex: Regex   = """^https://archiveofourown\.org:?/works/(\d+)(?:[/?].*)?$""".r

  def work(id: String): String = works.addPathPart(id).toString

  def series(id: String): String = series.addPathPart(id).toString

  def tag(name: String): String = tags.addPathPart(name).toString

  def tryParseFicId(url: String): Option[(FicType, String)] = url match
    case workIdRegex(id)   => Some((FicType.Work, id))
    case seriesIdRegex(id) => Some((FicType.Series, id))
    case _                 => None
}
