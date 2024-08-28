package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument
import com.kapibarabanka.ao3scrapper.StringUtils
import com.kapibarabanka.ao3scrapper.models.{Character, Fandom, FreeformTag}

case class TagDocument(FullName: Option[String], Name: String, Label: Option[String], WorkCount: Option[Int] = None)
    extends EntityDocument,
      WithWorkCount:
  def withFullName = this.copy(FullName = Some(StringUtils.combineWithLabel(this.Name, this.Label)), WorkCount = None)

object TagDocument:
  def fromCharacter(c: Character) = TagDocument(Some(StringUtils.combineWithLabel(c.name, c.label)), c.name, c.label)
  def fromFandom(c: Fandom)       = TagDocument(Some(StringUtils.combineWithLabel(c.name, c.label)), c.name, c.label)
  def fromTag(c: FreeformTag)     = TagDocument(None, c.name, None)
