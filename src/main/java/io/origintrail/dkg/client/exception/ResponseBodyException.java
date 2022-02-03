package io.origintrail.dkg.client.exception;

/**
 * Represents an error parsing the HTTP response body.
 */
public class ResponseBodyException extends DkgClientException {

    public ResponseBodyException(String message) {
        super(message);
    }

    public ResponseBodyException(String message, Throwable cause) {
        super(message, cause);
    }
}
