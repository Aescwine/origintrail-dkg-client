package io.origintrail.dkg.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON util class exposing helper methods for validating JSON objects
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    /**
     * Validates if the given {@code String} is valid JSON
     * @param jsonInString the String to validate
     * @return {@code true} if the given {@code String} is valid JSON, {@code false} otherwise.
     */
    public static boolean isJsonValid(String jsonInString) {
        try {
            MAPPER.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates if the given {@code byte[]} is valid JSON
     * @param jsonInByteArray the byte array to validate
     * @return {@code true} if the given {@code byte[]} is valid JSON, {@code false} otherwise.
     */
    public static boolean isJsonValid(byte[] jsonInByteArray) {
        try {
            MAPPER.readTree(jsonInByteArray);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
