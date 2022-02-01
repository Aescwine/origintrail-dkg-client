package io.origintrail.dkg.client.model;

import lombok.Getter;

@Getter
public enum SparqlQueryType {
    CONSTRUCT("construct");

    private final String value;

    SparqlQueryType(String value) {
        this.value = value;
    }
}
