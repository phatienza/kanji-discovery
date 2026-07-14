package dev.paulatienza.kanjidiscovery.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KanjiData(
        @JsonProperty("char") String character,
        List<String> parts,
        List<String> recipe,
        String meaning,
        String on,
        String kun,
        String jlpt,
        int strokes) {
}
