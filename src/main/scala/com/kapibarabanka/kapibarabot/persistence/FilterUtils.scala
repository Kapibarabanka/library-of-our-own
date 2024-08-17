package com.kapibarabanka.kapibarabot.persistence

import com.kapibarabanka.airtable.FilteredRequest

object FilterUtils {
  def filterByAo3Id(id: String): FilteredRequest = FilteredRequest(s"{ao3Id} = $id")
}
