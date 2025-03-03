package kapibarabanka.lo3.api
package services.ao3Info.internal

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document

protected[ao3Info] case class TagHtml(doc: Document):
  val name          = doc >> text("h2.heading")
  val canonicalName = doc >?> element("div.merger") >> text("a.tag")
  val isFilterable  = (doc >> element("div.tag") >> text("p")).contains("You can use it to filter works")
