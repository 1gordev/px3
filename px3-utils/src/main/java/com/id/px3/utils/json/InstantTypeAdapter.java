package com.id.px3.utils.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toEpochMilli());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        try {
            //  try to parse the value as long (milliseconds)
            return Instant.ofEpochMilli(json.getAsLong());
        } catch (NumberFormatException e) {
            try {
                //  if it's not a long, try to parse it as an ISO date-time string
                return Instant.parse(json.getAsString());
            } catch (DateTimeParseException ex) {
                throw new JsonParseException("Unable to parse value as either a long or an ISO date-time string: " + json, ex);
            }
        }
    }
}
