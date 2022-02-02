package io.origintrail.dkg.client.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an RDF N-Quad.
 */
@Getter
public class NQuad {
    private final String subject;
    private final String predicate;
    private final String object;
    private final String graph;

    public NQuad(String subject, String predicate, String object, String graph) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public static NQuad.NQuadBuilder builder(String subject, String predicate, String object) {
        return new NQuad.NQuadBuilder(subject, predicate, object);
    }

    @Override
    @JsonValue
    public String toString() {
        String nQuad =  subject + " " + predicate + " " + object;

        if (StringUtils.isNotBlank(graph)) {
            nQuad  = nQuad + " " + graph;
        }
        return nQuad + " .";
    }

    public static class NQuadBuilder {
        private final String subject;
        private final String predicate;
        private final String object;

        private String graph;

        public NQuadBuilder(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        public NQuadBuilder graph(String graph) {
            this.graph = graph;
            return this;
        }

        public NQuad build() {
            return new NQuad(subject, predicate, object, graph);
        }
    }
}
