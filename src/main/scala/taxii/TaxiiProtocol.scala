package taxii

import com.kodekutters.stix.Timestamp
import play.api.libs.json._

/**
  * [1] Trusted Automated Exchange of Intelligence Information (TAXII™) is an application layer protocol
  * for the communication of cyber threat information in a simple and scalable manner.
  *
  * reference: https://oasis-open.github.io/cti-documentation/
  */

sealed trait Taxii2

/**
  * The discovery resource contains information about a TAXII Server,
  * such as a human-readable title, description, and contact information,
  * as well as a list of API Roots that it is advertising.
  * It also has an indication of which API Root it considers the default,
  * or the one to use in the absence of other information/user choice.
  */
case class TaxiiDiscovery(title: String,
                          description: Option[String] = None,
                          contact: Option[String] = None,
                          default: Option[String] = None,
                          api_roots: Option[List[String]] = None) extends Taxii2 {

  val resourceName = TaxiiDiscovery.resourceName
}

object TaxiiDiscovery {
  val resourceName = "discovery"
  implicit val fmt = Json.format[TaxiiDiscovery]
}

/**
  * The api-root resource contains general information about the API Root,
  * such as a human-readable title and description, the TAXII versions it supports,
  * and the maximum size of the content body it will accept in a PUT or
  * POST (max_content_length).
  */
case class TaxiiApiRoot(title: String,
                        versions: List[String],
                        max_content_length: Int,
                        description: Option[String] = None) extends Taxii2 {

  val resourceName: String = TaxiiApiRoot.resourceName
}

object TaxiiApiRoot {
  val resourceName = "api-root"
  implicit val fmt = Json.format[TaxiiApiRoot]
}

/**
  * This type represents an object that was not added to the Collection.
  */
case class TaxiiStatusFailure(id: String, message: Option[String]) extends Taxii2 {
  val resourceName = TaxiiStatusFailure.resourceName // <--- todo not really a resource name
}

object TaxiiStatusFailure {
  val resourceName = "status-failure"
  implicit val fmt = Json.format[TaxiiStatusFailure]
}

/**
  * The status resource represents information about a request to add objects to a Collection.
  */
case class TaxiiStatus(id: String,
                       status: String,
                       total_count: Int,
                       success_count: Int,
                       failure_count: Int,
                       pending_count: Int,
                       request_timestamp: Option[Timestamp] = None,
                       failures: Option[List[TaxiiStatusFailure]] = None,
                       pendings: Option[List[String]] = None,
                       successes: Option[List[String]] = None) extends Taxii2 {

  val resourceName = TaxiiStatus.resourceName
}

object TaxiiStatus {
  val resourceName = "status"
  implicit val fmt = Json.format[TaxiiStatus]
}

/**
  * The collection resource contains general information about a Collection,
  * such as its id, a human-readable title and description,
  * an optional list of supported media_types
  * (representing the media type of objects can be requested from or added to it),
  * and whether the TAXII Client, as authenticated, can get objects from
  * the Collection and/or add objects to it.
  */
case class TaxiiCollection(id: String,
                           title: String,
                           can_read: Boolean,
                           can_write: Boolean,
                           description: Option[String] = None,
                           media_types: Option[List[String]] = None) extends Taxii2 {

  val resourceName = TaxiiCollection.resourceName
}

object TaxiiCollection {
  val resourceName = "collection"
  implicit val fmt = Json.format[TaxiiCollection]
}

/**
  * The collections resource is a simple wrapper around a list of collection resources.
  */
case class TaxiiCollections(collections: Option[List[TaxiiCollection]] = None) extends Taxii2 {

  val resourceName = TaxiiCollections.resourceName
}

object TaxiiCollections {
  val resourceName = "collections"
  implicit val fmt = Json.format[TaxiiCollections]
}

/**
  * The manifest-entry type captures metadata about a single object, indicated by the id property.
  */
case class TaxiiManifestEntry(id: String, // <--- UUID
                              date_added: Option[Timestamp],
                              versions: Option[List[String]] = None,
                              media_types: Option[List[String]] = None) extends Taxii2 {

  val resourceName = TaxiiManifestEntry.resourceName
}

object TaxiiManifestEntry {
  val resourceName = "manifest-entry"
  implicit val fmt = Json.format[TaxiiManifestEntry]
}

/**
  * The manifest resource is a simple wrapper around a list of manifest-entry items.
  */
case class TaxiiManifest(objects: Option[List[TaxiiManifestEntry]]) extends Taxii2 {
  val resourceName = TaxiiManifest.resourceName
}

object TaxiiManifest {
  val resourceName = "manifest"
  implicit val fmt = Json.format[TaxiiManifest]
}

/**
  * The error message is provided by TAXII Servers in the response body when
  * returning an HTTP error status and contains more information describing the error,
  * including a human-readable title and description, an error_code and error_id,
  * and a details structure to capture further structured information about the error.
  */
case class TaxiiErrorMessage(title: String,
                             description: Option[String] = None,
                             error_id: Option[String] = None,
                             error_code: Option[String] = None,
                             http_status: Option[String] = None,
                             external_details: Option[String] = None,
                             details: Option[Map[String, String]] = None) extends Taxii2 {

  val resourceName = TaxiiErrorMessage.resourceName // <--- todo not really a resource name
}

object TaxiiErrorMessage {
  val resourceName = "error"
  implicit val fmt = Json.format[TaxiiErrorMessage]
}

