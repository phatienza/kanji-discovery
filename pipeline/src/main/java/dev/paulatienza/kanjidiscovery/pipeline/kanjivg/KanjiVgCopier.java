package dev.paulatienza.kanjidiscovery.pipeline.kanjivg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

public final class KanjiVgCopier {
    public void copy(Path sourceDirectory, Path outputDirectory, Collection<String> characters)
            throws IOException {
        Files.createDirectories(outputDirectory);
        for (String character : characters) {
            if (character.codePointCount(0, character.length()) != 1) {
                throw new IOException("KanjiVG character must be one code point: " + character);
            }
            String filename = String.format("%05x.svg", character.codePointAt(0));
            Path source = sourceDirectory.resolve(filename);
            if (!Files.isRegularFile(source)) {
                throw new IOException("Missing KanjiVG SVG for " + character + ": " + source);
            }
            Files.copy(source, outputDirectory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
