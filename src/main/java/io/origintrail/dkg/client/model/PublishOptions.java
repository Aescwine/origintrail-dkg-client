package io.origintrail.dkg.client.model;

import lombok.Getter;
import lombok.ToString;

/**
 * Class for encapsulating DKG POST /publish request properties.
 */
@Getter
@ToString
public class PublishOptions {

    private final String assets;
    private final String keywords;
    private final boolean visibility;

    PublishOptions(String assets, String keywords, boolean visibility) {
        this.assets = assets;
        this.keywords = keywords;
        this.visibility = visibility;
    }

    public static PublishOptionsBuilder builder(String assets) {
        return new PublishOptionsBuilder(assets);
    }

    public static class PublishOptionsBuilder {
        private final String assets;
        private String keywords;
        private boolean visibility = true;

        PublishOptionsBuilder(String assets) {
            this.assets = assets;
        }

        public PublishOptionsBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public PublishOptionsBuilder visibility(boolean visibility) {
            this.visibility = visibility;
            return this;
        }

        public PublishOptions build() {
            return new PublishOptions(assets, keywords, visibility);
        }

        @Override
        public String toString() {
            return "PublishOptionsBuilder{" +
                    "assets='" + assets + '\'' +
                    ", keywords='" + keywords + '\'' +
                    ", visibility=" + visibility +
                    '}';
        }
    }
}
