package dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict;

import dev.paulatienza.kanjidiscovery.pipeline.parser.ParseStats;

import java.util.Map;

public record JmdictParseResult(Map<String, JmdictWord> entries, ParseStats stats) {
}
