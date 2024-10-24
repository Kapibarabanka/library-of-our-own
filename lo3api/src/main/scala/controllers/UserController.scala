package kapibarabanka.lo3.api
package controllers

import sqlite.SqliteError

import zio.http.*
import zio.http.codec.*
import zio.http.codec.PathCodec.*
import zio.http.endpoint.*
import zio.http.endpoint.EndpointMiddleware.None

protected[api] object UserController extends Controller:
  override protected val path: String = "user"

  private val allIdsEndpoint =
    Endpoint(RoutePattern.GET / path / "allIds")
      .out[List[String]]
      .outError[SqliteError](Status.InternalServerError)

  private val allIdsRoute =
    allIdsEndpoint.implement { case () => db.users.getAllIds }

  override val endpoints: List[Endpoint[?, ?, ?, ?, ?]] = List(allIdsEndpoint)
  override val routes: Routes[Any, Response]            = Routes(allIdsRoute)
