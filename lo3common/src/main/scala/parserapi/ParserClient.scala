package kapibarabanka.lo3.common
package parserapi

import models.domain.Lo3Error

import kapibarabanka.lo3.common.models.ao3.Ao3Error
import zio.http.{Method, Status}
import zio.http.codec.HttpCodec
import zio.schema.{DeriveSchema, Schema}

case class TestWork(id: String, title: String)
object TestWork:
  implicit val schema: Schema[TestWork] = DeriveSchema.gen

object ParserClient extends MyClient:
  override protected val clientName = "parser"

  val work = endpoint(Method.GET, "work")
    .query(HttpCodec.query[String]("id"))
    .out[TestWork]
    .outError[Lo3Error](Status.NotFound)

  val source = endpoint(Method.GET, "source")
    .query(HttpCodec.query[String]("url"))
    .out[String]
//    .outError[Ao3Error](Status.BadRequest)

  override val allEndpoints = List(work, source)
