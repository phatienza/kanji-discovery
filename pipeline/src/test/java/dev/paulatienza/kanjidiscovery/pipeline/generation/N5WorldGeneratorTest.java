package dev.paulatienza.kanjidiscovery.pipeline.generation;

import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class N5WorldGeneratorTest {
    @Test
    void generatesTheRealN5WorldAndRequiredRecipes() throws Exception {
        PipelinePaths paths = PipelinePaths.fromProjectRoot(Path.of("..").toAbsolutePath().normalize());

        WorldData world = new N5WorldGenerator().generate(paths);
        Map<String, KanjiData> kanji = world.kanji().stream()
                .collect(Collectors.toMap(KanjiData::character, Function.identity()));

        assertEquals(103, world.kanji().stream().filter(value -> "N5".equals(value.jlpt())).count());
        assertTrue(world.components().size() >= 200 && world.components().size() <= 220,
                () -> "component count=" + world.components().size());
        assertEquals(List.of("日", "月"), kanji.get("明").recipe());
        assertEquals(List.of("木", "木"), kanji.get("林").recipe());
        assertEquals(List.of("女", "子"), kanji.get("好").recipe());
        assertEquals(List.of("田", "力"), kanji.get("男").recipe());
        assertEquals(List.of("人", "木"), kanji.get("休").recipe());
        assertEquals(List.of("人", "木"), kanji.get("休").parts());
        assertTrue(world.words().stream().anyMatch(word -> word.word().equals("日本語")));
    }
}
