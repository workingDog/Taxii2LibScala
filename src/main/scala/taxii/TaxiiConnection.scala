package taxii

import com.kodekutters.stix.Bundle
import java.net.{URL, URLEncoder}
import java.util.Base64
import java.nio.charset.StandardCharsets

import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.language.postfixOps
import play.api.libs.ws.ahc._
import play.shaded.ahc.org.asynchttpclient.Response
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.WSAuthScheme

import scala.language.implicitConversions
import reflect.runtime.universe._
import play.api.libs.ws.DefaultBodyReadables._

import scala.concurrent.ExecutionContext.Implicits._


object TaxiiConnection {
  // create an Akka system for thread and streaming management
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // shutdown the ActorSystem properly
  def closeSystem(): Unit = {
    system.terminate()
  }

  //  def encodeURIComponent(uri: String): String = {
  //    // ref: https://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu
  //    try {
  //      URLEncoder.encode(uri, "UTF-8")
  //        .replaceAll("\\%28", "%28")
  //        .replaceAll("\\%29", "%29")
  //        .replaceAll("\\+", "%20")
  //        .replaceAll("\\%27", "'")
  //        .replaceAll("\\%21", "!")
  //        .replaceAll("\\%7E", "~")
  //    } catch {
  //      case e: Exception => uri
  //    }
  //  }

  /**
    * convert a filter to a query string.
    */
  //  def asQueryString(filter: Seq[String, String]): String = {
  //    (for (k <- filter.keys) yield {
  //      val value = if (k.equals("added_after")) k else "match[" + k + "]"
  //      encodeURIComponent(value) + '=' + encodeURIComponent(filter(k))
  //    }).mkString("&")
  //  }

}

/**
  * an https connection to a Taxii2 server
  *
  * for example: https://test.freetaxii.com:8000
  *
  * @param host     the host string
  * @param port     the port number, as an Integer
  * @param protocol the protocol, either http or https (default)
  * @param user     the user login name
  * @param password the user login password
  * @param timeout  in seconds, default 5 seconds
  */
case class TaxiiConnection(host: String,
                           port: Int,
                           protocol: String = "https",
                           user: String,
                           password: String,
                           timeout: Int = 5) {

  def this(url: URL, user: String, password: String, timeout: Int) = this(url.getHost, url.getPort, url.getProtocol, user, password, timeout)

  def this(url: String, user: String, password: String, timeout: Int) = this(new URL(url), user, password, timeout)

  import TaxiiConnection._

  // make sure have a clean protocol value
  val protocolValue = if (protocol.trim.endsWith(":")) protocol.trim.dropRight(1) else protocol.trim

  val baseURL = protocolValue.toLowerCase + "://" + host.trim + (if (port.toString.isEmpty) "" else ":" + port.toString.trim)

  val hash = Base64.getEncoder.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))

  val getHeaders = Map(
    "Accept" -> "application/vnd.oasis.taxii+json",
    "version" -> "2.0",
    "Authorization" -> ("Basic " + hash),
    "Content-Type" -> "application/vnd.oasis.taxii+json").toSeq

  val postHeaders = Map(
    "Accept" -> "application/vnd.oasis.taxii+json",
    "version" -> "2.0",
    "Authorization" -> ("Basic " + hash),
    "Content-Type" -> "application/vnd.oasis.stix+json").toSeq

  val stixHeaders = Map(
    "Accept" -> "application/vnd.oasis.stix+json",
    "version" -> "2.0",
    "Authorization" -> ("Basic " + hash),
    "Content-Type" -> "application/vnd.oasis.stix+json").toSeq

  // create the standalone WS client
  val wsClient = StandaloneAhcWSClient()

  /**
    * fetch data from the server. A GET with the chosen path is sent to the taxii server.
    * The json server response is parsed then converted to a Taxii2 protocol resource.
    *
    * @param thePath the url path for the GET
    * @tparam T the type of taxii2 resource to GET
    * @return either a future Taxii2 error message or a future Taxii2 [T] type resource.
    */
  def fetch[T: TypeTag](thePath: String, theHeaders: Seq[(String, String)] = getHeaders,
                        filter: Option[Seq[(String, String)]] = None): Future[Either[TaxiiErrorMessage, T]] = {

    // println(s"--> in fetch thePath $thePath")
    wsClient.url(thePath)
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withHttpHeaders(theHeaders: _*)
      .withRequestTimeout(timeout second)
      .withQueryStringParameters(filter.getOrElse(Seq.empty): _*)
      .get().map { response =>
      val js = response.body[JsValue]
      if (response.status == 200) {
        jsonToTaxii[T](js).asOpt match {
          case Some(taxiiObj) =>
            taxiiObj match {
              case x: TaxiiErrorMessage => Left(x)
              case x => Right(x.asInstanceOf[T])
            }
          case None => Left(TaxiiErrorMessage("fetch failed: cannot deserialize response"))
        }
      } else {
        Left(TaxiiErrorMessage(s"fetch failed with response code ${response.status}")) // the error message
      }
    }.recover({
      case e: Exception => Left(TaxiiErrorMessage("could not connect to: " + thePath, Option(e.getMessage)))
    })
  }

  /**
    * convert a json value to a Taxii2 or Bundle STIX object
    */
  private def jsonToTaxii[T: TypeTag](js: JsValue) = {
    typeOf[T] match {
      case x if x == typeOf[TaxiiDiscovery] => TaxiiDiscovery.fmt.reads(js)
      case x if x == typeOf[TaxiiApiRoot] => TaxiiApiRoot.fmt.reads(js)
      case x if x == typeOf[TaxiiStatusFailure] => TaxiiStatusFailure.fmt.reads(js)
      case x if x == typeOf[TaxiiStatus] => TaxiiStatus.fmt.reads(js)
      case x if x == typeOf[TaxiiCollection] => TaxiiCollection.fmt.reads(js)
      case x if x == typeOf[TaxiiCollections] => TaxiiCollections.fmt.reads(js)
      case x if x == typeOf[TaxiiManifestEntry] => TaxiiManifestEntry.fmt.reads(js)
      case x if x == typeOf[TaxiiErrorMessage] => TaxiiErrorMessage.fmt.reads(js)
      case x if x == typeOf[TaxiiManifest] => TaxiiManifest.fmt.reads(js)
      case x if x == typeOf[Bundle] => Bundle.fmt.reads(js)
    }
  }

  /**
    * post data to the server. A POST with the chosen path is sent to the taxii server.
    * The server response is converted a Taxii2 Status resource.
    *
    * @param thePath   the url path for the post
    * @param jsonValue the JsValue data to send as a json Taxii resource.
    * @return either a future Taxii2 error message or a future Taxii2 Status resource.
    *         Json.stringify(jsonValue)
    */
  def post(thePath: String, jsonValue: JsValue): Future[Either[TaxiiErrorMessage, TaxiiStatus]] = {
    wsClient.url(thePath)
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withHttpHeaders(postHeaders: _*)
      .withRequestTimeout(timeout second)
      .post(jsonValue).map { response =>
      val js = response.body[JsValue]
      if (response.status == 200) {
        jsonToTaxii[TaxiiStatus](js).asOpt match {
          case Some(status) =>
            status match {
              case x: TaxiiErrorMessage => Left(x)
              case x => Right(x.asInstanceOf[TaxiiStatus])
            }
          case None => Left(TaxiiErrorMessage("post failed: cannot deserialize response"))
        }
      } else {
        Left(TaxiiErrorMessage(s"post failed with response code ${response.status}"))
      }
    }.recover({
      case e: Exception => Left(TaxiiErrorMessage(s"post failed with error ${e.getMessage}"))
    })
  }

  /**
    * the client connection needs to be closed properly after use
    */
  def close(): Unit = {
    wsClient.close()
  }

}
