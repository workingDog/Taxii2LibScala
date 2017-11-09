package taxii

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/*
 method = GET
 url = /taxii/
 Parameters = none
 Pagination = no
 Filtering = no
 withHeaders("Accept" -> "application/vnd.oasis.taxii+json", "version" -> "2.0")

Successful Response:
  Status: 200 (OK)
  Content-Type: application/vnd.oasis.taxii+json; version=2.0
  Body: Discovery

 Common Error Codes: 401, 403, 404
*/

/**
  * This Endpoint provides general information about a TAXII Server, including the advertised API Roots.
  *
  * @param conn the connection to the taxii2 server
  */
case class Server(thePath: String = "/taxii/", conn: TaxiiConnection) {

  def this(url: String, user: String, password: String, timeout: Int) = this(conn = new TaxiiConnection(url, user, password, timeout))

  // request the discovery resource from the server --> will get the resource or an error message.
  val response: Future[Either[TaxiiErrorMessage, TaxiiDiscovery]] = conn.fetch[TaxiiDiscovery](conn.baseURL + thePath)
  // extract the discovery resource from the response
  val discovery: Future[Option[TaxiiDiscovery]] = response.map(_.toOption)
  // extract the error message from the response
  val errorMessage: Future[Option[TaxiiErrorMessage]] = response.map(Left(_).toOption)

  // request the api-roots from the server
  val apiRootList: Future[List[ApiRoot]] =
    discovery.map({
      case Some(d) => d.api_roots.map(rootList =>
        for (apiRoot <- rootList) yield ApiRoot(apiRoot, conn)).getOrElse(List.empty)
      case None => List.empty
    })

  // convenience methods
  def apiRoots(ndx: Int): Future[ApiRoot] = apiRootList.map(x => x(ndx))

  def apiRoots(): Future[List[ApiRoot]] = apiRootList

  def api_roots(ndx: Int): Future[String] = apiRootList.map(x => x(ndx).api_root)

  def api_roots(): Future[List[String]] = apiRootList.map(theList => for (x <- theList) yield x.api_root)

}
