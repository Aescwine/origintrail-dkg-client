# Java DKG API client

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

Execute all unit tests with the maven command: 
```
mvn test
```

### Integration tests

`DkgClientIntegrationTest` contains the DKG API integration tests, including an end to end flow, covering all API endpoints. <p/>Run integration tests locally:  
```
mvn integration-test
```

Note: update the host and port to your local node if required

# Usage

The Java DKG client library is asynchronous, using `java.net.http.HttpClient` for HTTP requests. 

API responses are wrapped in a **CompletableFuture** containing the result of the API request, to facilitate non-blocking logic within your code.

If using Spring Boot/Reactor Core, you can create a **Mono** from **CompletableFuture** if necessary:
```java
Mono monoResult = reactor.core.publisher.Mono.fromFuture(result);
```
        
If required, block the `CompletableFuture` to wait for the result:

```java
CompletableFuture<HandlerId> publishHandlerId = dkgClient.publish(fileName, fileData, publishOptions);
HandlerId handler = publishHandlerId.join() // block and wait for result
```

### API response objects

Where possible, model classes have been created to map an API response to Java objects, allowing for easier handling of the result data.<p/> In instances where the response data is dynamic or unknown, `JsonNode` objects are used to represent the JSON response data.

### Exception Handling

When expected process flow is interrupted, **DKGClient** takes the approach of throwing unchecked exceptions of abstract type `DkgClientException`, rather than propagating exceptions up the stack.

Any exceptions thrown during `CompletableFuture` completion, are wrapped within exceptions of type `CompletionException`, which can be accessed with `ex.getCause()`.

#### DKG Client exception types:

- `RequestValidationException` - exception occurred creating request.
- `UriCreationException` - exception creating request Uri.
- `HttpResponseException` - unsuccessful HTTP response status.
    - Includes access to the response **statusCode**, and a **reasonPhrase** (taken from the response body).
- `ResponseBodyException` - exception parsing response body.
- `UnexpectedException` - unexpected request/response processing exception.

## Creating a DkgClient

The class `DkgClient` is the entrypoint for executing requests against the Decentralized Knowledge Graph.

To open a connection to a node and start querying, simply create an instance of the `DkgClient`:

```java
DkgClient dkgClient = new DkgClient();

// or create with a HOST and PORT if different from the default of localhost and 8900.
DkgClient dkgClient = new DkgClient(HOST, PORT);
```

### Using the client

#### Get node information
```java
CompletableFuture<NodeInfo> nodeInfo = dkgClient.info();
```

#### Publish an assertion
```java
// if you have the assertion JSON byte array:
CompletableFuture<HandlerId> handlerId = dkgClient.publish(fileName, fileData, publishOptions);

// or if you want to publish from a file path
String filePath = "/root/some-assertion-file.json";
CompletableFuture<HandlerId> handlerId = dkgClient.publish(filePath, publishOptions);

---
// get the publish result  
CompletableFuture<PublishResult> publishResult = dkgClient.getPublishResult(handlerId.getHandlerId());

// retrieve assertion id when future completes
publishResult.thenApply(result -> result.getData().getId());
```

#### Resolve an assertion
```java
// takes a List<String> of assertion ids to resolve
CompletableFuture<HandlerId> handlerId = dkgClient.resolve(Collections.singletonList(assertionId));

// get result
CompletableFuture<ResolveResult> resolveResult = dkgClient.getResolveResult(handlerId.getHandlerId());
```

**More examples TBC**

### How to process a `CompletableFuture` response object

One approach to processing a `CompletableFuture` on completion, would be to utilise the `CompletableFuture<U> handle` method. This gives you access to the result and potential exception of the current completable future. You can then transform the result or handle the exception as required.

For example, the below code will return the `id` of the returned publish result, or send the exception message to the system output stream.

```
dkgClient.getPublishResult(handlerId.getHandlerId())
    .handle((result, ex) -> {
        if (ex != null) {
            System.out.println(ex.getMessage);
        } else {
            return result.getData().getId();
        }
    }
);
```

## Logging Integration

The DKGClient library uses [SLF4J](https://www.slf4j.org/) to allow implementing applications to use their own logging framework.

Configure the **DKGClient** logging level within your logging framework configuration, for example:

```
<logger name="io.origintrail.dkg.client" level="WARN" />
```
