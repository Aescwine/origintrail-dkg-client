package io.origintrail.dkg.client.exception;

import lombok.Getter;
import lombok.ToString;
import org.apache.hc.core5.util.TextUtils;

@Getter
@ToString
public class HttpResponseException extends DkgClientException {

    private final int statusCode;
    private final String reasonPhrase;

    public HttpResponseException(final int statusCode, final String reasonPhrase) {
        super(String.format("status code: %d" +
                (TextUtils.isBlank(reasonPhrase) ? "" : ", reason phrase: %s"), statusCode, reasonPhrase));
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }
}