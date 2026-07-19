package dev.paulatienza.kanjidiscovery.pipeline.curation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VariantTable {
    private final Map<String, String> variants;

    private VariantTable(Map<String, String> variants) {
        this.variants = Map.copyOf(variants);
    }

    public static VariantTable load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !"variant\tcanonical\tstatus\tnote".equals(lines.get(0))) {
            throw new IOException("Variant table has an invalid header: " + path);
        }
        Map<String, String> variants = new LinkedHashMap<>();
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] columns = line.split("\\t", -1);
            if (columns.length != 4 || columns[0].isBlank() || columns[1].isBlank()) {
                throw new IOException("Malformed variant row " + (index + 1) + ": " + line);
            }
            if (!"CONFIRMED".equals(columns[2])) {
                throw new IOException("Unreviewed variant row " + (index + 1)
                        + " has status " + columns[2] + ": " + columns[0]);
            }
            if (variants.putIfAbsent(columns[0], columns[1]) != null) {
                throw new IOException("Duplicate radical variant at row " + (index + 1) + ": " + columns[0]);
            }
        }
        return new VariantTable(variants);
    }

    public String canonicalize(String component) {
        return variants.getOrDefault(component, component);
    }
}
