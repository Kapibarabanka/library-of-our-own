package kapibarabanka.lo3.api
package controllers

import zio.http.*

protected[api] trait Controller:
  val routes: List[Route[Any, Response]]
