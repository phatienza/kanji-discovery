package dev.paulatienza.kanjidiscovery.pipeline.model;

import java.util.List;

public record WorldData(
        List<ComponentData> components,
        List<KanjiData> kanji,
        List<WordData> words) {
}
