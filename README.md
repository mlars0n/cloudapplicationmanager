# Cloud Application Manager #
Cloud Application Manager is a Java/Vaadin web application to assist in pulling together cloud resources that 
make up an application. It requires a database (MySQL supported so far). It allows you to track metadata about an 
application and its environments, and do testing and health checks against them. 

## Build and locally ##
Building this application requires Gradle, which can be bootstrapped by the application code itself.

JDK 11+ required to build. Make sure this is set up and available on the command line.

Commands:

* Build: ./gradlew
* java  -Dconfig.file=<PATH_TO_PROPS_FILE> -jar <PROJECT_HOME>/build/libs/*-SNAPSHOT.jar

## Run in a container ##

* Build: docker build -t cloudapplicationmanager .
* Run: docker run --env-file=<PROPERTIES_FILE> -p 8080:8080 cloudapplicationmanager
  * You may have to add DNS to get addresses to resolve. This will let you reach a database on localhost:
    * --network="host"
    * Or you can add the address of a DNS server, like this:
      * --dns <DNS_SERVER>

