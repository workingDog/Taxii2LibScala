package com.kodekutters.taxii

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This Endpoint provides information about the Collections hosted under this API Root.
  *
  * @param conn the connection to the taxii server
  */
case class Collections(api_root: String, conn: TaxiiConnection) {

  val thePath = api_root + "collections/"

  // request the Collections resource from the server --> will get the resource or an error message.
  lazy val response: Future[Either[TaxiiErrorMessage, TaxiiCollections]] = conn.fetch[TaxiiCollections](thePath)
  // extract the Collections resource from the response
  lazy val taxiiCollections: Future[Option[TaxiiCollections]] = response.map(_.toOption)
  // extract the error message from the response
  lazy val errorMessage: Future[Option[TaxiiErrorMessage]] = response.map(Left(_).toOption)

  lazy val collectionList: Future[List[Collection]] = {
    taxiiCollections.map({
      case Some(cols) => cols.collections.map(colList =>
        for (colx <- colList) yield Collection(colx, api_root, conn)).getOrElse(List.empty)
      case None => List.empty
    })
  }

  // convenience methods
  def collections(range: String = ""): Future[List[Collection]] = {
    val theHeader = if (range == null || range.isEmpty) conn.getHeaders
    else (conn.getHeaders.toMap + ("Range" -> ("items=" + range))).toSeq
    conn.fetch[TaxiiCollections](thePath, theHeader).map(_.toOption match {
      case Some(cols) => cols.collections.map(colList =>
        for (colx <- colList) yield Collection(colx, api_root, conn)).getOrElse(List.empty)
      case None => List.empty
    })
  }

  def collections(index: Int): Future[Collection] = collectionList.map(x => x(index))

  def get(index: Int): Future[Collection] = collectionList.map(theList => theList(index))

}
