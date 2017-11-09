package taxii

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/*
 method = GET
 url = /<api-root>/
 Parameters = <api-root> - the base URL of the API Root containing the Collections
 Pagination = no
 Filtering = no
 withHeaders("Accept" -> "application/vnd.oasis.taxii+json", "version" -> "2.0")

Successful Response:
  Status: 200 (OK)
  Content-Type: application/vnd.oasis.taxii+json; version=2.0
  Body: ApiRoot

 Common Error Codes: 401, 403, 404
*/

/**
  * This Endpoint provides general information about an API Root,
  * which can be used to help users and clients decide whether and how they want to interact with it.
  *
  * @param api_root the api_root path of this ApiRootRequest
  * @param conn     the connection to the taxii server
  */
case class ApiRoot(api_root: String, conn: TaxiiConnection) {

  // request the ApiRoot resource from the server --> will get the resource or an error message.
  val response: Future[Either[TaxiiErrorMessage, TaxiiApiRoot]] = conn.fetch[TaxiiApiRoot](api_root)
  // extract the ApiRoot resource from the response
  val apiRoot: Future[Option[TaxiiApiRoot]] = response.map(_.toOption)
  // extract the error message from the response
  val errorMessage: Future[Option[TaxiiErrorMessage]] = response.map(Left(_).toOption)

  // convenience methods
  def collections(index: Int): Future[Collection] = Collections(api_root, conn).collections(index)

  def collections(): Future[List[Collection]] = Collections(api_root, conn).collections()

  def status(status_id: String): Future[Option[TaxiiStatus]] = Status(api_root, status_id, conn).get()

}
