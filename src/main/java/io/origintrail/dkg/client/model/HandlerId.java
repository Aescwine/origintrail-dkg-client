package io.origintrail.dkg.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a DKG API response {@code handler_id}.
 * The handler id is used in subsequent API requests to retrieve the result of the request which returned this id.
 * See {@link io.origintrail.dkg.client.api.DkgClient#resolve(String)}
 * and {@link io.origintrail.dkg.client.api.DkgClient#getResolveResult(String)} as an example.
 */
@Getter
@Setter
@ToString
public class HandlerId {

    @JsonProperty("handler_id")
    private String handlerId;
}
