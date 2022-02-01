package io.origintrail.dkg.client.http;

import lombok.Getter;

@Getter
public enum HttpMediaType {

    APPLICATION_JSON("application/json; charset=utf-8"),
    APPLICATION_JSON_LD("application/ld+json; charset=utf-8"),
    MULTIPART_FORM_DATA("multipart/form-data");

    private final String value;

    HttpMediaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
