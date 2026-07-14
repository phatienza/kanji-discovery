package dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2;

import java.util.List;

public record KanjidicEntry(
        String character,
        Integer grade,
        Integer legacyJlpt,
        int strokes,
        Integer frequency,
        List<String> onReadings,
        List<String> kunReadings,
        List<String> meanings) {
}
