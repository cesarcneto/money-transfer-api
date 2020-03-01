# Money Transfer API

The goal is to design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.

## Side goals
* Learn how to make use of Javalin webframework (first project attempt)
* Learn how to make use of JDBI (first project attempt - From their github: *"The Jdbi library provides convenient, idiomatic access to relational databases in Java"*) 

## Design decisions and assumptions

### Design decisions
- The [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) approach is appreciated
- With the Clean Architecture in mind, this is the motivation to not use [JSON-P](https://javaee.github.io/jsonp/index.html) as the JSON processor library because of the fact that the goal is to translate the actual JSON input into data transfer objects (DTOs) first. And, in order to reduce the amount of boilerplate code a more convenient library like Jackson have been chosen.
- There plenty of JSON processor libraries in the market and it was not the goal in project to pick the "best" option for this use case - the selection have been mostly based on the developer previous knowledge and experience 
- Following the Clean Architecture approach, Mapstruct have been used to enable the construction of between-layers adapters
- Lombok has been picked over libraries like AutoValue or Immutables due to personal convenience regarding previous knowledge background. One should be aware of the risks involved in using it as described [here](https://medium.com/@vgonzalo/dont-use-lombok-672418daa819)

### Assumptions
- It has been assumed that there is no need to store the history of transactions (known as `TransferRequest` in the current design)
- Most of the tests have been developed as Integration tests simply because of the fact that, effort-wise, they would represent the same. And since the project till now is small enough, one should not expect a slow feedback loop while developing and/or testing
- It's ok for this example to not provide an OpenAPI spec

### API

#### Accounts

##### POST `/accounts`

Request payload
```json
{
  "initialBalance" : "10.00"
}
```

Response payload - Http status: `CREATED - 201`
```json
{
  "id" : "eef35112-f5b0-44e0-8cb9-b39f08bea1de",
  "initialBalance" : "10.00"
}
```

##### GET `/accounts/{accountId}`

Response payload - Http status: `OK - 200`
```json
{
  "id" : "eef35112-f5b0-44e0-8cb9-b39f08bea1de",
  "initialBalance" : "10.00"
}
```

#### Transfers API

##### POST `/transfers`

Request payload
```json
{
	"from" : "eef35112-f5b0-44e0-8cb9-b39f08bea1de",
	"to" : "af296a22-9289-4270-b92f-de599a862bef",
	"purpose" : "thanks for the coffee",
	"amount" : "1.00",
	"occurredAt" : "2020-02-29T13:00:00.000Z"
}
```

Response payload - Http status: `OK - 200`

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
* [Javalin](https://javalin.io/)
* [JDBI](https://github.com/jdbi/jdbi)
* [jackson-databind](https://github.com/FasterXML/jackson-databind)
* [Project Lombok](https://projectlombok.org/)
* [MapStruct](https://mapstruct.org/)
* [JUnit 5](https://junit.org/junit5/)
* [Mockito framework](https://site.mockito.org/)
* [Gradle Build Tool](https://gradle.org/)