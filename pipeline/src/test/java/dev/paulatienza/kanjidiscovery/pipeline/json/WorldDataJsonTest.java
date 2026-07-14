package dev.paulatienza.kanjidiscovery.pipeline.json;

import dev.paulatienza.kanjidiscovery.pipeline.model.ComponentData;
import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WordData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorldDataJsonTest {
    @Test
    void roundTripsTheV1ContractAndIgnoresFutureFields() throws Exception {
        WorldData source = new WorldData(
                List.of(new ComponentData("日", "sun, day", "ニチ", "ひ", 4)),
                List.of(new KanjiData("曜", List.of("日", "翟"), null,
                        "weekday", "ヨウ", "", "N5", 18)),
                List.of(new WordData("明日", List.of("明", "日"),
                        "あした", "tomorrow", "N5")));

        String json = WorldDataJson.mapper().writeValueAsString(source);
        WorldData decoded = WorldDataJson.mapper().readValue(
                json.replaceFirst("\\{", "{\"future\":true,"), WorldData.class);

        assertEquals("日", decoded.components().get(0).character());
        assertEquals(List.of("日", "翟"), decoded.kanji().get(0).parts());
        assertNull(decoded.kanji().get(0).recipe());
        assertEquals("あした", decoded.words().get(0).reading());
    }
}
