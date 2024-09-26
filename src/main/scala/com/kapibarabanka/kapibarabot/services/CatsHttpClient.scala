package com.kapibarabanka.kapibarabot.services

import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import zio.interop.catz.*
import zio.{Task, ZIO, ZLayer}

trait CatsHttpClient:
  val http: Client[Task]

case class CatsHttpClientImpl(http: Client[Task]) extends CatsHttpClient

object CatsHttpClientImpl:
  def layer = ZLayer(
    for {
      catsHttpClient       <- BlazeClientBuilder[Task].resource.toScopedZIO
      catsClientWithLogger <- ZIO.succeed(Logger(logBody = true, logHeaders = true)(catsHttpClient))
    } yield CatsHttpClientImpl(catsClientWithLogger)
  )
