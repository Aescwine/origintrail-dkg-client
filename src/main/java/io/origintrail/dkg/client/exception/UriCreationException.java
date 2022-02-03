package io.origintrail.dkg.client.exception;

public class UriCreationException extends DkgClientException {

    public UriCreationException(String msg) {
        super(msg);
    }

    public UriCreationException(String msg, Throwable th) {
        super(msg, th);
    }
}
