package dev.paulatienza.kanjidiscovery.pipeline.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;

import java.io.IOException;
import java.nio.file.Path;

public final class WorldDataJson {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private WorldDataJson() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static WorldData read(Path path) throws IOException {
        return MAPPER.readValue(path.toFile(), WorldData.class);
    }

    public static void write(Path path, WorldData worldData) throws IOException {
        MAPPER.writeValue(path.toFile(), worldData);
    }
}
