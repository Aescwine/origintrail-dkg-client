package io.origintrail.dkg.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MultiPartData {
    private final String contentType;
    private final byte[] data;
}
