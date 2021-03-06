package com.kodekutters.taxii

import com.kodekutters.stix.Bundle
import java.net.URL
import java.util.Base64
import java.nio.charset.StandardCharsets

import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.StandaloneWSResponse

import scala.language.postfixOps
import play.api.libs.ws.ahc._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.WSAuthScheme

import scala.language.implicitConversions
import reflect.runtime.universe._

import scala.concurrent.ExecutionContext.Implicits._


/**
  * an https connection to a Taxii-2.0 server
  */
object TaxiiConnection {
  var taxiiVersion = "2.0"

  val mediaType_stix = "application/vnd.oasis.stix+json"
  val mediaType_taxii = "application/vnd.oasis.taxii+json"

  // create an Akka system for thread and streaming management
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // shutdown the ActorSystem properly
  def closeSystem(): Unit = {
    system.terminate()
  }
}

/**
  * an https connection to a Taxii-2.0 server
  *
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

  // private val logger = Logger(classOf[TaxiiConnection])

  private val portString = if (port.toString.isEmpty || (port == -1)) "" else ":" + port.toString.trim

  // make sure have a clean protocol value
  private val protocolValue = if (protocol.trim.endsWith(":")) protocol.trim.dropRight(1) else protocol.trim

  val baseURL = protocolValue.toLowerCase + "://" + host.trim + portString

  val hash = Base64.getEncoder.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))

  val getHeaders = Map(
    "Accept" -> mediaType_taxii,
    "version" -> taxiiVersion).toSeq

  val postHeaders = Map(
    "Accept" -> mediaType_taxii,
    "Content-Type" -> mediaType_stix,
    "version" -> taxiiVersion).toSeq

  val stixHeaders = Map(
    "Accept" -> mediaType_stix,
    "version" -> taxiiVersion).toSeq

  // create the standalone Web Service client
  val wsClient = StandaloneAhcWSClient()

  /**
    * fetch data from the server. A GET with the chosen path is sent to the Taxii-2.0 server.
    * The json server response is parsed then converted to a Taxii-2.0 protocol resource.
    *
    * @param thePath the url path for the GET
    * @tparam T the type of Taxii-2.0 resource to GET
    * @return either a future Taxii-2.0 error message or a future Taxii-2.0 [T] type resource.
    */
  def fetch[T: TypeTag](thePath: String, theHeaders: Seq[(String, String)] = getHeaders,
                        filter: Option[Seq[(String, String)]] = None): Future[Either[TaxiiErrorMessage, T]] = {

    wsClient.url(thePath)
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withHttpHeaders(theHeaders: _*)
      //  .withRequestFilter(AhcCurlRequestLogger(logger.underlying))
      .withRequestTimeout(timeout second)
      .withQueryStringParameters(filter.getOrElse(Seq.empty): _*)
      .get().map { response =>
      response.status match {
        // partial content
        case 206 => getTaxiiObject[T](response)
        // todo aggregate the partial content
        //          val contentRangeOpt = response.header("Content-Range")
        //          contentRangeOpt.map(r => {
        //            val rangeTuple = toRangeInfo(r)
        //            println("----> r: " + r + " start: " + rangeTuple._1 + " end: " + rangeTuple._2 + " total: " + rangeTuple._3)
        //          })

        // all results if the server can deliver without pagination
        case 200 => getTaxiiObject[T](response)

        case _ => Left(TaxiiErrorMessage(s"fetch failed with response code ${response.status}"))
      }
    }.recover({
      case e: Exception => Left(TaxiiErrorMessage("could not connect to: " + thePath, Option(e.getMessage)))
    })
  }

  // a crude way to replace the booleans given as string to real booleans
  private def adjustBooleans(js: JsValue): JsValue = {
    val newJs = js.toString()
      .replaceAll(""": *(?i)"true"""", """:true""")
      .replaceAll(""": *(?i)"false"""", """:false""")
    Json.parse(newJs)
  }

  private def getTaxiiObject[T: TypeTag](response: StandaloneWSResponse): Either[TaxiiErrorMessage, T] = {
    val js = response.body[JsValue]
  //  println("-----> response js: " + js)
  //  val jsx = adjustBooleans(jsTemp)
    jsonToTaxii[T](js).asOpt match {
      case Some(taxiiObj) =>
        taxiiObj match {
          case x: TaxiiErrorMessage => Left(x)
          case x => Right(x.asInstanceOf[T])
        }
      case None => Left(TaxiiErrorMessage("fetch failed: cannot deserialize response"))
    }
  }

  /** retrieve the raw response from the connection
    * e.g.  conn.getRawResponse("https://.....").map { response =>
    *         println("-----> response status: " + response.status + " body: " + response.body)
    *       }
    */
  def getRawResponse(thePath: String, theHeaders: Seq[(String, String)] = getHeaders,
                     filter: Option[Seq[(String, String)]] = None): Future[StandaloneWSResponse] = {

    wsClient.url(thePath)
      .withAuth(user, password, WSAuthScheme.BASIC)
      .withHttpHeaders(theHeaders: _*)
      .withRequestTimeout(timeout second)
      .withQueryStringParameters(filter.getOrElse(Seq.empty): _*)
      .get()
  }

  /**
    * convert a json value to a Taxii-2.0 or Bundle STIX object
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
    * The server response is converted a Taxii-2.0 Status resource.
    *
    * @param thePath   the url path for the post
    * @param jsonValue the JsValue data to send as a json Taxii-2.0 resource.
    * @return either a future Taxii-2.0 error message or a future Taxii-2.0 Status resource.
    *
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

  /**
    * return the Content-Range string as a tuple of 3 integers,
    * start, end and total of the range
    * If an error occurs return (-1,-1,-1)
    *
    * @param theString the Content-Range  string, for example "items 0-10/58"
    */
  def toRangeInfo(theString: String): Tuple3[Int, Int, Int] = {
    try {
      val part = theString.replace("items ", "").split("/")
      val theRange = part(0).split("-")
      val start = theRange(0).toInt
      val end = theRange(1).toInt
      val total = part(1).toInt
      (start, end, total)
    } catch {
      case ex: Throwable => (-1, -1, -1)
    }
  }
}
