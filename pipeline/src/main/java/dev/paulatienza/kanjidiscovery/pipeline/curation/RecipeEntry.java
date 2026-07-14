package dev.paulatienza.kanjidiscovery.pipeline.curation;

import java.util.List;

public record RecipeEntry(String kanji, List<String> recipe, String status, String note) {
}
