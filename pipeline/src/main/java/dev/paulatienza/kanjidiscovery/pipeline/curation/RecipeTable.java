package dev.paulatienza.kanjidiscovery.pipeline.curation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecipeTable {
    private final Map<String, RecipeEntry> entries;

    private RecipeTable(Map<String, RecipeEntry> entries) {
        this.entries = Map.copyOf(entries);
    }

    public static RecipeTable load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !"kanji\trecipe\tstatus\tnote".equals(lines.get(0))) {
            throw new IOException("Recipe table has an invalid header: " + path);
        }
        Map<String, RecipeEntry> entries = new LinkedHashMap<>();
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] columns = line.split("\\t", -1);
            if (columns.length != 4 || columns[0].isBlank() || columns[1].isBlank()) {
                throw new IOException("Malformed recipe row " + (index + 1) + ": " + line);
            }
            List<String> recipe = Arrays.stream(columns[1].split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            if (recipe.size() < 2) {
                throw new IOException("Recipe must have at least two parts at row " + (index + 1));
            }
            RecipeEntry entry = new RecipeEntry(columns[0], recipe, columns[2], columns[3]);
            if (entries.putIfAbsent(columns[0], entry) != null) {
                throw new IOException("Duplicate recipe for " + columns[0] + " at row " + (index + 1));
            }
        }
        return new RecipeTable(entries);
    }

    public List<String> recipeFor(String kanji) {
        RecipeEntry entry = entries.get(kanji);
        return entry == null ? null : entry.recipe();
    }

    public Map<String, RecipeEntry> entries() {
        return entries;
    }
}
