package dev.paulatienza.kanjidiscovery.pipeline.model;

import java.util.List;

public record WordData(
        String word,
        List<String> parts,
        String reading,
        String meaning,
        String jlpt) {
}
