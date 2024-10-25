package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.models.openapi.UserClient
import zio.http.*

protected[api] object UserController extends Controller:
  private val add = UserClient.add.implement { (id, username) => db.users.addUser(id, username) }

  private val allIds = UserClient.allIds.implement { Unit => db.users.getAllIds }
  
  private val setEmail = UserClient.setEmail.implement { (id, email) => db.users.setKindleEmail(id, email) }

  override val routes: List[Route[Any, Response]] = List(add, allIds, setEmail)
