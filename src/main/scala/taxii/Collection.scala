package taxii

import com.kodekutters.stix.Bundle
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This Endpoint provides general information about a Collection, which can be used to help
  * users and clients decide whether and how they want to interact with it.
  *
  * @param conn the connection to the taxii server
  */
case class Collection(taxiiCollection: TaxiiCollection, api_root: String,
                      conn: TaxiiConnection = TaxiiConnection("", 0, "", "", "")) {

  def this(url: String, id: String, api_root: String, user: String, password: String) =
    this(new TaxiiCollection(id, "temp", true, true), api_root, new TaxiiConnection(url, user, password, 5))

  def this(id: String, api_root: String, user: String, password: String) =
    this(new TaxiiCollection(id, "temp", true, true), api_root, new TaxiiConnection(api_root, user, password, 5))

  def this(id: String, api_root: String) = this(new TaxiiCollection(id, "temp", true, true), api_root)


  val basePath = api_root + "collections/" + taxiiCollection.id
  val thePath = basePath + "/objects/"

  def getObjects(filter: Option[Seq[(String, String)]] = None): Future[Option[Bundle]] =
    conn.fetch[Bundle](thePath, conn.stixHeaders, filter).map(_.toOption)

  def getObject(obj_id: String, filter: Option[Seq[(String, String)]] = None): Future[Option[Bundle]] =
    conn.fetch[Bundle](thePath + obj_id + "/", conn.stixHeaders, filter).map(_.toOption)

  def addObjects(bundle: Bundle): Future[Option[TaxiiStatus]] =
    conn.post(thePath, Json.toJson[Bundle](bundle)).map(_.toOption)

  def getManifest(filter: Option[Seq[(String, String)]] = None): Future[Option[TaxiiManifest]] = {
    val thePath = basePath + "/manifest/"
    conn.fetch[TaxiiManifest](thePath, conn.getHeaders, filter).map(_.toOption)
  }

}
