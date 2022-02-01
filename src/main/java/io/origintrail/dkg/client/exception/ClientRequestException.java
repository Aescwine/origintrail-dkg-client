package io.origintrail.dkg.client.exception;

public class ClientRequestException extends DkgClientException{

    public ClientRequestException(String message) {
        super(message);
    }

    public ClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
