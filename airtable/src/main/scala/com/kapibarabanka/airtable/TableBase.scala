package com.kapibarabanka.airtable

import com.kapibarabanka.airtable.AirtableError.*
import cats.syntax.all.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.Method.*
import org.http4s.Status.{NotFound, Successful, UnprocessableEntity}
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.jsonEncoder
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.implicits.uri
import org.http4s.{AuthScheme, Credentials, Method, Request, Uri}
import zio.interop.catz.*
import zio.{IO, Task, ZIO}

abstract class TableBase[A <: EntityDocument: io.circe.Decoder: io.circe.Encoder](
    tableName: String,
    token: String,
    http: Client[Task]
) extends Table[A]:
  private val auth                  = Authorization(Credentials.Token(AuthScheme.Bearer, token))
  private val baseUrl               = uri"https://api.airtable.com/v0/appx80f0D1MARDMO3/"
  protected val url: Uri            = baseUrl.addPath(tableName)
  protected val listRecordsUrl: Uri = url.addPath("listRecords")

  protected def runRequest[TResp](request: Request[Task])(implicit d: Decoder[TResp]): IO[AirtableError, TResp] = {
    http
      .run(request)
      .use {
        case NotFound(resp) => resp.as[String].flatMap(msg => ZIO.fail(NotFoundError(msg, request.uri.renderString)))
        case UnprocessableEntity(resp) =>
          resp.as[String].flatMap(msg => ZIO.fail(UnprocessableEntityError(msg, request.uri.renderString)))
        case Successful(resp) => resp.as[TResp]
        case resp             => resp.as[String].flatMap(msg => ZIO.fail(UnclassifiedError(msg, request.uri.renderString)))
      }
      .logError
      .mapError(e => ParsingError(e.getMessage, request.uri.renderString))
  }

  override def patch(record: Record[A]): IO[AirtableError, Record[A]] = {
    val json    = PatchRequest(List(record)).asJson.deepDropNullValues.dropEmptyValues
    val request = constructRequest(PATCH, url, json)
    runRequest[RecordsResponse[A]](request).map(r => r.records.last)
  }

  override def upsert(records: List[A]): IO[AirtableError, List[Record[A]]] = {
    val chunks = records.distinct.sliding(10, 10).toList

    def upsertChunk(chunk: List[A]) = {
      // TODO: research asJson.deepDropNullValues.dropEmptyValues maybe write custom encoder-decoder
      val json    = UpsertRequest(chunk.map(a => Record(None, a)), upsertParameters).asJson.deepDropNullValues.dropEmptyValues
      val request = constructRequest(PATCH, url, json)
      runRequest[RecordsResponse[A]](request)
    }

    chunks.parTraverse(upsertChunk).map(_.flatMap(response => response.records))
  }

  override def upsert(doc: A): IO[AirtableError, Record[A]] = for {
    response <- upsert(List(doc))
  } yield response.last

  override def find(id: String): IO[AirtableError, Record[A]] = {
    val request = constructRequest(GET, url.addPath(id))
    runRequest[Record[A]](request)
  }

  override def findOption(id: String): IO[AirtableError, Option[Record[A]]] = {
    find(id).map(r => Some(r)).catchSome { case _: NotFoundError => ZIO.succeed(None) }
  }

  override def delete(id: String): IO[AirtableError, Unit] = {
    val request = constructRequest(DELETE, url.addPath(id))
    runRequest[String](request).map(r => ())
  }

  override def delete(ids: List[String]): IO[AirtableError, Unit] = {
    val chunks = ids.distinct.sliding(10, 10).toList

    def deleteChunk(chunk: List[String]) = {
      val request = constructRequest(DELETE, url.withMultiValueQueryParams(Map("records" -> chunk)))
      runRequest[String](request).map(r => ())
    }

    chunks.parTraverse(deleteChunk).map(_ => {})
  }

  override def filter(filter: FilteredRequest): IO[AirtableError, List[Record[A]]] = {
    val request = constructRequest(POST, listRecordsUrl, filter.asJson)
    getWithOffset(request, List())
  }

  override def getAll: IO[AirtableError, List[Record[A]]] = {
    val request = constructRequest(GET, url)
    getWithOffset(request, List())
  }

  protected def constructRequest(method: Method, uri: Uri): Request[Task] =
    Request[Task]().withMethod(method).withUri(uri).withHeaders(auth)

  protected def constructRequest(method: Method, uri: Uri, body: Json): Request[Task] =
    constructRequest(method, uri).withEntity(body)

  protected def getWithOffset(request: Request[Task], records: List[Record[A]]): IO[AirtableError, List[Record[A]]] =
    runRequest[RecordsResponse[A]](request).flatMap(response =>
      response.offset match
        case None => ZIO.succeed(records ++ response.records)
        case Some(value) =>
          getWithOffset(request.withUri(request.uri.withQueryParam("offset", value)), records ++ response.records)
    )
