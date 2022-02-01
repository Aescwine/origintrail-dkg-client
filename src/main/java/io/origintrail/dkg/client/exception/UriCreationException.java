package io.origintrail.dkg.client.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UriCreationException extends DkgClientException {

    public UriCreationException(String msg) {
        super(msg);
    }

    public UriCreationException(String msg, Throwable th) {
        super(msg, th);
    }
}
