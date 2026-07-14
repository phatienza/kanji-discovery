package dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Kanjidic2ParserTest {
    @Test
    void streamsJapaneseReadingsEnglishMeaningsAndLevelData() throws Exception {
        Path fixture = Path.of("src/test/resources/fixtures/kanjidic2-minimal.xml");

        KanjidicParseResult result = new Kanjidic2Parser().parse(fixture);
        KanjidicEntry day = result.entries().get("日");

        assertEquals(2, result.stats().accepted());
        assertEquals(1, day.grade());
        assertEquals(4, day.legacyJlpt());
        assertEquals(4, day.strokes());
        assertEquals("ニチ", day.onReadings().get(0));
        assertEquals("ひ", day.kunReadings().get(0));
        assertEquals(java.util.List.of("day", "sun"), day.meanings());
    }

    @Test
    void failsOnCharacterWithoutLiteral(@TempDir Path tempDir) throws Exception {
        Path malformed = tempDir.resolve("bad.xml");
        Files.writeString(malformed, "<kanjidic2><character><misc><stroke_count>1</stroke_count></misc></character></kanjidic2>");

        assertThrows(KanjidicFormatException.class, () -> new Kanjidic2Parser().parse(malformed));
    }
}
