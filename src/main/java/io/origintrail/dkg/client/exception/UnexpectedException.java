package io.origintrail.dkg.client.exception;

public class UnexpectedException extends DkgClientException{

    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
