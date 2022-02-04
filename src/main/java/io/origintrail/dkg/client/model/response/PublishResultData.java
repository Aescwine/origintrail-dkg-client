package io.origintrail.dkg.client.model.response;

import lombok.Data;

import java.util.List;

@Data
public class PublishResultData {

    private String id;
    private String rootHash;
    private String signature;
    private Metadata metadata;
    private Assertion assertion;
    private List<String> rdf;
    private String message;
}
