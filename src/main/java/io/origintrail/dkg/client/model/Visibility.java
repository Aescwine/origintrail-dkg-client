package io.origintrail.dkg.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Visibility {
    PUBLIC("public"),
    PRIVATE("private");

    private final String value;
}
