package io.origintrail.dkg.client.model;

import lombok.Getter;
import lombok.ToString;

/**
 * {@code HttpUrlOptions} used for configuring http connection options.
 */
@Getter
@ToString
public class HttpUrlOptions {

    private final String scheme;
    private final String host;
    private final int port;

    public HttpUrlOptions(String host, int port, String scheme) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }
}
