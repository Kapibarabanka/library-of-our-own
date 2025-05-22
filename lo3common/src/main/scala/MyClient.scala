package kapibarabanka.lo3.common

import models.ao3.FicType
import models.domain.UserFicKey

import zio.http.RoutePattern.fromMethod
import zio.http.codec.{HttpCodec, PathCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, RoutePattern, Status}

trait MyClient:
  protected val clientName: String

  protected def endpoint[T](method: Method, routePattern: PathCodec[T]) =
    Endpoint(fromMethod(method) / clientName / routePattern).tag(clientName)

  protected def withStringError[PathInput, Input, Output, Auth <: AuthType](
      endpoint: Endpoint[PathInput, Input, zio.ZNothing, Output, Auth]
  ) = endpoint
    .outError[String](Status.InternalServerError)

  protected def withKey[PathInput, Err, Output, Auth <: AuthType](
      endpoint: Endpoint[PathInput, Unit, Err, Output, Auth]
  ) = endpoint
    .query(HttpCodec.query[String]("userId"))
    .query(HttpCodec.query[String]("ficId"))
    .query(HttpCodec.query[String]("ficType"))
    .transformIn[UserFicKey] { case (userId, ficId, ficType) =>
      UserFicKey(userId, ficId, FicType.valueOf(ficType.toLowerCase.capitalize))
    } { key =>
      (key.userId, key.ficId, key.ficType.toString)
    }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]]
