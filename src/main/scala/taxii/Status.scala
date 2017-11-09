package taxii

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/*
 method = GET
 url = 	/<api-root>/status/<status-id>/
 Parameters =
<api-root> - the base URL of the API Root containing the Collections
<status-id> - the identifier of the status message being requested
 Pagination = no
 Filtering = no
 withHeaders("Accept" -> "application/vnd.oasis.taxii+json", "version" -> "2.0")

Successful Response:
  Status: 200 (OK)
  Content-Type: application/vnd.oasis.taxii+json; version=2.0
  Body: Status

 Common Error Codes: 401, 403, 404
*/

/**
  * This Endpoint provides information about the status of a previous request.
  *
  * @param conn the connection to the taxii server
  */
case class Status(api_root: String, status_id: String, conn: TaxiiConnection) {

  val thePath = api_root + "/status/" + status_id + "/"

  // request the Status resource from the server --> will get the resource or an error message.
  val response: Future[Either[TaxiiErrorMessage, TaxiiStatus]] = conn.fetch[TaxiiStatus](thePath)
  // extract the Status resource from the response
  val taxiiStatus: Future[Option[TaxiiStatus]] = response.map(_.toOption)
  // extract the error message from the response
  val errorMessage: Future[Option[TaxiiErrorMessage]] = response.map(Left(_).toOption)

  def get(): Future[Option[TaxiiStatus]] = taxiiStatus

}



