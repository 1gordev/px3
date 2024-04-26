package com.id.px3.utils.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toMillis());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        try {
            // Try to parse the value as long (milliseconds)
            return Duration.ofMillis(json.getAsLong());
        } catch (NumberFormatException e) {
            try {
                // If it's not a long, try to parse it as an ISO-8601 duration string
                return Duration.parse(json.getAsString());
            } catch (DateTimeParseException ex) {
                throw new JsonParseException("Unable to parse value as either a long (milliseconds) or an ISO-8601 duration string: " + json, ex);
            }
        }
    }
}
