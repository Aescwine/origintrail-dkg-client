package io.origintrail.dkg.client.http;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The {@code MultiPartBody} provides the ability through the use of {@code MultiPartBodyBuilder}
 * to create a {@code HttpRequest.BodyPublisher} representing an HTTP multipart/form-data request body.
 */
public class MultiPartBody {

    private MultiPartBody() {};

    public static MultiPartBodyBuilder builder() {
        return new MultiPartBodyBuilder();
    }

    public static class MultiPartBodyBuilder {

        private final List<byte[]> partList = new ArrayList<>();
        private final String boundary = UUID.randomUUID().toString();

        public String getBoundary() {
            return boundary;
        }

        public HttpRequest.BodyPublisher build() {
            if (partList.size() == 0) {
                throw new IllegalStateException("Must have at least one part to build multipart message.");
            }
            addFinalBoundaryPart();

            return HttpRequest.BodyPublishers.ofByteArrays(partList);
        }

        public MultiPartBodyBuilder addPart(String name, String value) {
            String part = getBoundaryStart(name) + "\r\n\r\n" + value + "\r\n";

            partList.add(part.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        public MultiPartBodyBuilder addPart(String name, String value, String contentType) {
            String part = getBoundaryStart(name) + "\r\n"
                    + "Content-Type: " + contentType + "\r\n\r\n" + value + "\r\n";

            partList.add(part.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        public MultiPartBodyBuilder addFilePart(String name, String fileName, MultiPartData data) {
            String filePartHeader = getBoundaryStart(name)
                    + "; filename=" + fileName + "\r\n"
                    + "Content-Type: " + data.getContentType() + "\r\n\r\n";

            partList.add(filePartHeader.getBytes(StandardCharsets.UTF_8));
            partList.add(data.getData());
            partList.add("\r\n".getBytes(StandardCharsets.UTF_8));
            return this;
        }

        private String getBoundaryStart(String name) {
            return  "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=" + name;
        }

        private void addFinalBoundaryPart() {
            String part = "--" + boundary + "--";
            partList.add(part.getBytes(StandardCharsets.UTF_8));
        }
    }
}
