Change Log
==========


### changes in 0.7-SNAPSHOT

* updated scala to 2.13.0, sbt to 1.3.0 and associated plugins and dependencies


### changes in 0.6

* added getRawResponse(..) in TaxiiConnection to retrieve the raw response from a connection
* updated dependency scalastix to 1.0


### changes in 0.5

* change all Int (32 bit) to Long (64 bit), because the specs require 64 integers
* updated dependency scalastix to 0.9

### changes in 0.4

* updated scala, sbt and associated dependencies

### changes in 0.3

* removed the "Authorization" -> ("Basic " + hash) from all headers 
    (since already using .withAuth(user, password, WSAuthScheme.BASIC) in TaxiiConnection)

### changes in 0.2

* changed TaxiiConnection.taxiiVersion to a var.
* changed the dependency to scalastix-0.7 for STIX-2.0 specs.

### changes in 0.1

initial release (6 Dec 2017): 
https://mvnrepository.com/artifact/com.github.workingDog/taxii2lib_2.12/0.1

