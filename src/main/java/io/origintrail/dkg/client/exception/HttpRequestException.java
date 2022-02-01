package io.origintrail.dkg.client.exception;

public class HttpRequestException extends DkgClientException{

    public HttpRequestException(String message) {
        super(message);
    }

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
