# Money Transfer API: Code Challenge

The goal is to design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.

## Open questions

1. 

## Design decisions and assumptions

### Design decisions
- The [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) approach is appreciated
- With the Clean Architecture in mind, this is the motivation to not use [JSON-P](https://javaee.github.io/jsonp/index.html) as the JSON processor library because of the fact that the goal is to translate the actual JSON input into data transfer objects (DTOs) first. And, in order to reduce the amount of boilerplate code a more convenient library like Jackson have been chosen.
- There plenty of JSON processor libraries in the market and it was not the goal in project to pick the "best" option for this use case - the selection have been mostly based on the developer previous knowledge and experience 
- Following the Clean Architecture approach, Mapstruct have been used to enable the construction of between-layers adapters
- Lombok usage, through `@Value`, makes immutability easy to use in java
- Lombok has been picked over libraries like AutoValue or Immutables due to personal convenience regarding previous knowledge background. One should be aware of the risks involved in using it as described [here](https://medium.com/@vgonzalo/dont-use-lombok-672418daa819)

### Assumptions


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Basic requirements for the project are:

1. [Java - JDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)
2. [Gradle Build Tool](https://gradle.org/)
3. [Docker](https://www.docker.com/)

BTW, have you ever heard of [SDKMAN](https://sdkman.io/)? 
It's a Software Development Kit Manager that will make these installations a breeze.

### Build and Run

#### Using Docker

Assuming project's root folder is the current directory, the following steps are required in order to build and run this application using Docker, one should:

1. Build the multi-stage build container

```
docker build -t money-transfer-api:latest .
```

2. Run the container

```
docker run -d --rm --name money-transfer-api money-transfer-api:latest
```

#### Using Gradle and/or Java Runtime Environment

Assuming project's root folder is the current directory, the following steps are required in order to build and run this application

1. Build the application and run tests

```
gradle build
```

2. Run the application

For running the application, one can either run using Gradle's application run plugin or run the generated JAR file.
To run using Gradle's application run plugin:
```
gradle run
```

To run the generated JAR file:
```
java -jar build/libs/money-transfer-api-all.jar
```

## Built With

* [Java](https://www.java.com/en/)
* [jackson-databind](https://github.com/FasterXML/jackson-databind)
* [Project Lombok](https://projectlombok.org/)
* [MapStruct](https://mapstruct.org/)
* [JUnit 5](https://junit.org/junit5/)
* [Mockito framework](https://site.mockito.org/)
* [Gradle Build Tool](https://gradle.org/)