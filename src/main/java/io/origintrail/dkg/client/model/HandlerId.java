package io.origintrail.dkg.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.origintrail.dkg.client.DkgClient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Represents a DKG API response {@code handler_id}.
 * The handler id is used in subsequent API requests to retrieve the result of the request which returned this id.
 * See {@link DkgClient#resolve(List)}
 * and {@link DkgClient#getResolveResult(String)} as an example.
 */
@Getter
@Setter
@ToString
public class HandlerId {

    @JsonProperty("handler_id")
    private String handlerId;
}
