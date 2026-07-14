package dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile;

import dev.paulatienza.kanjidiscovery.pipeline.parser.ParseStats;

import java.util.Map;

public record KradParseResult(Map<String, KradEntry> entries, ParseStats stats) {
}
