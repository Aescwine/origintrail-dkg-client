package io.origintrail.dkg.client.exception;

public class RequestValidationException extends DkgClientException{

    public RequestValidationException(String message) {
        super(message);
    }

    public RequestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
