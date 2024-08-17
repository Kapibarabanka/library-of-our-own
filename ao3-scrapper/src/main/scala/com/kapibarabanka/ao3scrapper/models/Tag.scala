package com.kapibarabanka.ao3scrapper.models

/** Please be sure to create tags with [[Ao3Old.getCanonicalTagName]] for the correct handling of synonymous tags
 */
trait Tag:
  def name: String
  def category: TagCategory
