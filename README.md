#Recipe Backend

## Requirements

For building and running the application you need:

- [JDK 1.11](https://www.oracle.com/java/technologies/downloads/#java11)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `RecipesBackendApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

For dev environment:

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

For production environment:

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Running the application in Docker

Run following command

```shell
mvn spring-boot:build-image
```

**For this to work, we need to have Docker installed and running.**

Then to start the container, we can simply run:

```shell
docker run -it -p8080:8080 recipes-backend:0.0.1-SNAPSHOT
```