package com.kapibarabanka.ao3scrapper.docs

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document

case class TagDoc(doc: Document):
  val name          = doc >> text("h2.heading")
  val canonicalName = doc >?> element("div.merger") >> text("a.tag")
  val isFilterable  = (doc >> element("div.tag") >> text("p")).contains("You can use it to filter works")
