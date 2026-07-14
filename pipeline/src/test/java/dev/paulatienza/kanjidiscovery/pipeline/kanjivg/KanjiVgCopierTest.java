package dev.paulatienza.kanjidiscovery.pipeline.kanjivg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KanjiVgCopierTest {
    @Test
    void copiesFiveDigitUnicodeSvgAndFailsWhenMissing(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("source");
        Path output = tempDir.resolve("output");
        Files.createDirectories(source);
        Files.writeString(source.resolve("065e5.svg"), "<svg/>");

        new KanjiVgCopier().copy(source, output, List.of("日"));

        assertTrue(Files.isRegularFile(output.resolve("065e5.svg")));
        assertThrows(IOException.class,
                () -> new KanjiVgCopier().copy(source, output, List.of("月")));
    }
}
