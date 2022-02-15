package io.origintrail.dkg.client.model;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Class for encapsulating DKG POST /publish request properties.
 */
@Getter
@ToString
public class PublishOptions {

    private final List<String> keywords;
    private final Visibility visibility;
    private final String ual;

    PublishOptions(List<String> keywords, Visibility visibility, String ual) {
        this.keywords = keywords;
        this.visibility = visibility;
        this.ual = ual;
    }

    public static PublishOptionsBuilder builder(List<String> keywords) {
        return new PublishOptionsBuilder(keywords);
    }

    public static class PublishOptionsBuilder {
        private final List<String> keywords;
        private Visibility visibility = Visibility.PUBLIC;
        private String ual;

        PublishOptionsBuilder(List<String> keywords) {
            this.keywords = keywords;
        }

        public PublishOptionsBuilder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public PublishOptionsBuilder ual(String ual) {
            this.ual = ual;
            return this;
        }

        public PublishOptions build() {
            return new PublishOptions(keywords, visibility, ual);
        }
    }
}
