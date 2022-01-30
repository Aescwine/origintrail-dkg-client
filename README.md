# DKG API - Java library

An asynchronous Java library, providing an interface into the OriginTrail Decentralized Knowledge Graph, enabling:

- importing & publishing of data to the public DKG
- network and local querying of information based on topics and identifiers
- verifying the integrity of queried data
- exporting of datasets in different formats 

## Prerequisities and Dependencies

 ```
 Logging: SLF4J
 ```

## Installation

Build the library using the following maven command:

```
mvn clean install
```

## Usage

This library is asynchronous, using `java.net.http.HttpClient` for all API calls, returning a `CompletableFuture` containing the result of the API request.

```java
CompletableFuture<Info> result = dkgClient.info();
result.join() // block and wait for result
...
// wrap Future in Mono for futher async processing
Mono monoResult = reactor.core.publisher.Mono.fromFuture(result);
```

```java
// create instance of DkgClient, passing in HOST and PORT of your node
DkgClient dkgClient = new DkgClient(HOST, PORT);

// get info
CompletableFuture<Info> result = dkgClient.info();

```

More TBC

## Logging integration

TBC
