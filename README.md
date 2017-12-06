## TAXII 2.0 client library in scala

**taxii2Lib** is a [Scala](https://www.scala-lang.org/) library that 
provides a set of classes and methods for building clients to [TAXII 2.0](https://oasis-open.github.io/cti-documentation/) servers.

[[1]](https://oasis-open.github.io/cti-documentation/) 
Trusted Automated Exchange of Intelligence Information (TAXII) is an application layer protocol 
used to exchange cyber threat intelligence (CTI) over HTTPS. 
TAXII enables organizations to share CTI by defining an API that aligns with common sharing models.
[TAXII 2.0 Specification](https://oasis-open.github.io/cti-documentation/) defines the TAXII RESTful API and its resources along with the requirements for TAXII Client and Server implementations. 


**taxii2lib** uses asynchronous requests to fetch TAXII 2.0 server resources. 
It provides the following endpoints:

- *Server*, endpoint for retrieving the discovery and api roots resources.
- *ApiRoot*, endpoint for retrieving the api roots resources.
- *Collections*, endpoint for retrieving the list of collection resources. 
- *Collection*, endpoint for retrieving a collection resource and associated objects. 
- *Status*, endpoint for retrieving a status resource. 

### Usage

The following TAXII 2.0 API services are supported with these corresponding async methods:

- Server Discovery --> server.discovery 
- Get API Root Information --> server.api_roots(), server.api_roots(i)
- Get Collections --> collections.collections(range), collections.collections(i) and collections.get(i)
- Get Objects --> collection.getObjects(filter, range)
- Add Objects --> collection.addObject(bundle)
- Get an Object --> collection.getObject(obj_id, filter)
- Get Object Manifests --> collection.getManifests(filter, range)
- Get Status --> status.get()

The class *TaxiiConnection* provides the async communication to the server.

Example:

    import com.kodekutters.taxii._
    // a connection object with a 5 seconds timeout
    val conn = new TaxiiConnection("https://test.freetaxii.com:8000", "user-me", "mypassword", 5)
    val server = new Server("/com.kodekutters.taxii/", conn)
    server.discovery.map(d => println("---> discovery " + d))

See the [TAXII 2.0 Specification](https://oasis-open.github.io/cti-documentation/) for the list 
of attributes of the TAXII 2.0 server responses.

### Installation and packaging

To use the latest release add the following dependency to your build.sbt:

    libraryDependencies += "com.github.workingDog" %% "taxii2lib" % "0.1"

The best way to compile and package **taxii2lib** from source is to use [SBT](http://www.scala-sbt.org/).
To compile and generate a jar file from source:

    sbt package

This will produce a jar file "taxii2lib_2.12-0.2-SNAPSHOT.jar" in the "./target/scala-2.12" directory 
for use in Scala applications.


To publish the libraries to your local (Ivy) repository, simply type:

    sbt publishLocal

Then put this in your Scala app build.sbt file

    libraryDependencies += "com.github.workingDog" %% "taxii2lib" % "0.2-SNAPSHOT" 
 
### Dependencies and requirements

See *build.sbt* for the code dependencies.

Note: Java Cryptography Extension (JCE) Unlimited Strength is needed for TLS-1.2 https connections.
With Java 1.8.0_152 (and above) to enable TLS-1.2 use the following code: 

    Security.setProperty("crypto.policy", "unlimited")

With older Java you need to download the JCE from Oracle and follow the installation instructions.   
 
### References
 
1) [TAXII 2.0 Specification](https://oasis-open.github.io/cti-documentation/)
 

### Status
work inn progress, not tested.