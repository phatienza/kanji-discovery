package dev.paulatienza.kanjidiscovery.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ComponentData(
        @JsonProperty("char") String character,
        String meaning,
        String on,
        String kun,
        int strokes) {
}
