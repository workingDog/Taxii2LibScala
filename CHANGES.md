Change Log
==========


### changes in 0.4-SNAPSHOT



### changes in 0.3

* removed the "Authorization" -> ("Basic " + hash) from all headers 
    (since already using .withAuth(user, password, WSAuthScheme.BASIC) in TaxiiConnection)

### changes in 0.2

* changed TaxiiConnection.taxiiVersion to a var.
* changed the dependency to scalastix-0.7 for STIX-2.0 specs.

### changes in 0.1

initial release (6 Dec 2017): 
https://mvnrepository.com/artifact/com.github.workingDog/taxii2lib_2.12/0.1

