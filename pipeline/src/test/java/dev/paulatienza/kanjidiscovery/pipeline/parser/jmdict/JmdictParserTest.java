package dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JmdictParserTest {
    @Test
    void returnsOnlyRequestedCommonWordsWithPriorityReading() throws Exception {
        Path fixture = Path.of("src/test/resources/fixtures/JMdict-minimal.xml");

        JmdictParseResult result = new JmdictParser().parse(fixture, Set.of("明日", "珍語"));

        assertEquals("あした", result.entries().get("明日").reading());
        assertEquals("tomorrow", result.entries().get("明日").meaning());
        assertFalse(result.entries().containsKey("珍語"));
        assertEquals(2, result.stats().total());
        assertEquals(1, result.stats().accepted());
    }
}
