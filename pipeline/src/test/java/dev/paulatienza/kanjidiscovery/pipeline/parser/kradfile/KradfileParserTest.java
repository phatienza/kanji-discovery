package dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KradfileParserTest {
    @Test
    void explicitlyDecodesEucJpAndPreservesDuplicateParts() throws Exception {
        Path fixture = Path.of("src/test/resources/fixtures/kradfile-minimal.eucjp");

        KradParseResult result = new KradfileParser().parse(fixture);

        assertEquals(List.of("木", "木"), result.entries().get("林").parts());
        assertEquals(List.of("化", "木"), result.entries().get("休").parts());
        assertEquals(5, result.stats().accepted());
    }

    @Test
    void failsLoudlyOnMalformedDataLine(@TempDir Path tempDir) throws Exception {
        Path malformed = tempDir.resolve("kradfile");
        Files.write(malformed, "林 木 木\n".getBytes(java.nio.charset.Charset.forName("EUC-JP")));

        assertThrows(KradfileFormatException.class, () -> new KradfileParser().parse(malformed));
    }
}
