# DKG API - Java library

An asynchronous Java library, providing an interface into the OriginTrail Decentralized Knowledge Graph, enabling:

- importing & publishing of data to the public DKG
- network and local querying of information based on topics and identifiers
- verifying the integrity of queried data
- exporting of datasets in different formats 

**Compatible with DKG version**: *6.0.0-beta.1.20* - https://github.com/OriginTrail/ot-node

# Disclaimer

**This library is still in beta and under development.** Use at own risk of disappointment that it doesn't work as expected!

## Prerequisites and Dependencies

- Java 11+
- [Jackson Faster XML](https://github.com/FasterXML/jackson)
- [Apache Jena](https://jena.apache.org/) - SPARQL query building
- [Project Lombok](https://github.com/projectlombok/lombok)
- [SLF4J](https://www.slf4j.org/)

## Installation

Build the library using the following maven command:

```
mvn clean install
```

## Executing Tests

### Unit tests

Execute all unit tests with the maven command: `mvn test`

### Integration tests

`DkgApiIntegrationTest` contains the DKG API integration tests, including an end to end flow, covering all API endpoints. <p/>To run integration tests locally, run `mvn integration-test`

Note: update the host and port to your local node if required

## Usage

The **DKGClient** library is asynchronous, using `java.net.http.HttpClient` for HTTP requests. 

API responses are wrapped in a **CompletableFuture** containing the result of the API request, to facilitate non-blocking logic within your code.

If using Spring Boot/Reactor Core, create a **Mono** from **CompletableFuture**:
```java
Mono monoResult = reactor.core.publisher.Mono.fromFuture(result);
```
        
If required, block the `CompletableFuture` to wait for the result:

```java
CompletableFuture<HandlerId> publishHandlerId = dkgClient.publish(fileName, fileData, publishOptions);
HandlerId handler = publishHandlerId.join() // block and wait for result
```

### DkgClient

The class `DkgClient` is the entrypoint for executing requests against the Decentralized Knowledge Graph.

```java
// create instance of DkgClient
DkgClient dkgClient = new DkgClient();
// or create with a HOST and PORT if different from the default of localhost and 8900.
DkgClient dkgClient = new DkgClient(HOST, PORT);
```
#### Get node info
```
CompletableFuture<NodeInfo> result = dkgClient.info();
```
Example response object:
```json
{
    "version": "6.0.0-beta.1.20",
    "auto_update": false,
    "telemetry": true
}
```

#### Publish assertion
```
// if you have the assertion JSON byte array:
CompletableFuture<HandlerId> publishHandlerId = dkgClient.publish(fileName, fileData, publishOptions);
// or if you want to publish from a file path:
CompletableFuture<HandlerId> publishHandlerId = dkgClient.publish(filePath, publishOptions);

```
Example response object:
```json
{
  "handler_id": "ffd8a00e-bf22-4432-8d88-804f4f9baa27"
}
```

#### Retrieve published assertion
```
CompletableFuture<JsonNode> publishResult = dkgClient.getPublishResult(publishHandlerId.getHandlerId());
// retrieve assertion id when future completes
publishResult.thenApply(r -> r.path("data").path("id").asText());
```

**More examples TBC**

## Exception Handling

When expected process flow is interrupted, **DKGClient** takes the approach of throwing unchecked exceptions of abstract type `DkgClientException`, rather than propagating exceptions up the stack.

`CompletableFuture` objects throw exceptions of type `CompletionException`, which wrap the underlying cause, which can be accessed with `ex.getCause()`.

Concrete exception types:
- `RequestValidationException` - exception occurred creating request.
- `UriCreationException` - exception creating request Uri.
- `HttpResponseException` - exception occurred processing HTTP response.
- `ResponseBodyException` - exception parsing response body.
- `UnexpectedException` - unexpected request/response processing exception.

## Logging Integration

The DKGClient library uses [SLF4J](https://www.slf4j.org/) to allow implementing applications to use their own logging framework.

Configure the **DKGClient** logging level within your logging framework configuration, for example:

```
<logger name="io.origintrail.dkg.client" level="WARN" />
```
