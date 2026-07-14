package dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2;

import dev.paulatienza.kanjidiscovery.pipeline.parser.ParseStats;

import java.util.Map;

public record KanjidicParseResult(Map<String, KanjidicEntry> entries, ParseStats stats) {
}
