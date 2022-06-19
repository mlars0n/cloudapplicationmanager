# Cloud Application Manager #
Cloud Application Manager is a Java/Spring Boot/Vaadin web application that allows you to track the cloud resources that 
make up an application. It allows you to discover and record metadata about a service's infrastructure and environments, 
and do testing and health checks against the metadata you've captured about your applications and services. The goal is to 
be able to see the cloud resources not just as individual pieces but rather as part of an entire service or application, in 
order to help operate, support, and monitor the service.

## What problems does this application solve? ##

If anyone has created a modern cloud application, it is likely to be composed of an application server and load balancer, at 
the very least. Add Kubernetes, microservices, and "serverless" components, and understanding and monitoring the flow
can get very complex, making it difficult to determine what is working and what is not, and how the pieces fit together. This 
tool's highest abstraction is the concept of a complete service within a cloud context, and will help make sense of what can 
often feel like a chaotic jumble of components and services.

## Cloud services supported ##

Currently, only AWS is supported.

## Dependencies ##

Running this app requires:
* Java or Docker (if running in a container)
* A relational database such as MySQL (MySQL only supported currently)

## Installation ##

### 1. Create your database ###

* Create a MySQL database along with credentials if needed. Any name works but if you need help deciding how about _cloudappmanager_?
* Choose your own USER_NAME (_camuser_?) and password.

```shell
mysql> CREATE DATABASE <DATABASE_NAME>;
mysql> CREATE USER '<USER_NAME>'@'localhost' IDENTIFIED WITH mysql_native_password BY '<PASSWORD>';
mysql> GRANT ALL ON cloudappmanager.* TO '<USER_NAME>'@'localhost';
```

Note that the schema will be created on application startup the first time via Liquibase 
(and any future updates will be applied on application startup as well if your database requires migration).

### 2. Create your configuration file ###
See the cloudappmanager.properties.sample file for an example file you can copy and fill in the correct
values. However, here are the properties that you must set:
```properties
spring.datasource.url=jdbc:mysql://<HOST_NAME>:<PORT>/<DATABASE_NAME>?autoreconnect=true 
spring.datasource.username=<USER_NAME>
spring.datasource.password=<PASSWORD>
```

If you use the Docker option you will set these as environment variables in the sample command below.

If you build the application yourself, you can pass the configuration file as system property.

### 3. Docker option (recommended): download and run the latest container build from Docker Hub ###

Note that to reach an external database you need to set the network to use the host settings as per the 
following command. The properties will become environment variables which will be usable by the container.


```shell
$ docker pull mlars0n/cloudapplicationmanager
$ docker run --env-file=<PROPERTIES_FILE> --network=host mlars0n/cloudapplicationmanager
```

As an alternative the --network=host you can set the address of a DNS server:
* --dns <DNS_SERVER>

### 4. Build and run locally option ###
Building this application requires Gradle, which can be bootstrapped by the application code itself.

JDK 11+ required to build. Make sure this is set up and available on the command line.

You will also need NPM installed (a recent version, 8.4+ or higher, is required).

Configuration: To configure the file, you can either set environment variables (as you would in a container environment),
or you can set a properties file Java system parameter:
* config.file

You can set that to point to the full path of your properties file as in the examples below.

Build commands:

* Build: ./gradlew
* java  -Dconfig.file=<PATH_TO_PROPS_FILE> -jar <PROJECT_HOME>/build/libs/*-SNAPSHOT.jar

Build the Docker image:
* Build: docker build -t cloudapplicationmanager .
    

