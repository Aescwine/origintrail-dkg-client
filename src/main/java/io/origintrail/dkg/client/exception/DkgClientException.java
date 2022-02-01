package io.origintrail.dkg.client.exception;

public abstract class DkgClientException extends RuntimeException {

    public DkgClientException(String message) {
        super(message);
    }

    public DkgClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
