package dev.paulatienza.kanjidiscovery.pipeline.validation;

import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WordData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import dev.paulatienza.kanjidiscovery.pipeline.scene.Scene;
import dev.paulatienza.kanjidiscovery.pipeline.scene.SceneData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneValidatorTest {
    @Test
    void acceptsCumulativeDependenciesAndPrintsCoverage() {
        WorldData world = new WorldData(List.of(), List.of(
                kanji("日", List.of(), null),
                kanji("月", List.of(), null),
                kanji("明", List.of("日", "月"), List.of("日", "月"))
        ), List.of(new WordData("明日", List.of("明", "日"), "あした", "tomorrow", "N5")));
        SceneData scenes = new SceneData(List.of(
                new Scene("sky", "The sky", "sky.svg", List.of("日", "月"),
                        List.of("明"), List.of("明日"), 0.7)
        ));

        ValidationResult result = new SceneValidator().validate(world, scenes, "N5");

        assertTrue(result.errors().isEmpty(), () -> String.join("\n", result.errors()));
        assertTrue(result.report().contains("sky: total=3 find=2 craft=1 irregular=0"));
        assertTrue(result.report().contains("N5 coverage: 3/3 assigned exactly once"));
    }

    @Test
    void rejectsUnavailableRecipeDuplicateAssignmentAndUnavailableWordPart() {
        WorldData world = new WorldData(List.of(), List.of(
                kanji("日", List.of(), null),
                kanji("明", List.of("日", "月"), List.of("日", "月"))
        ), List.of(new WordData("明日", List.of("明", "日"), "あした", "tomorrow", "N5")));
        SceneData scenes = new SceneData(List.of(
                new Scene("one", "One", "one.svg", List.of("日"), List.of("明"), List.of(), 0.7),
                new Scene("two", "Two", "two.svg", List.of("日"), List.of(), List.of("明日"), 0.7)
        ));

        ValidationResult result = new SceneValidator().validate(world, scenes, "N5");

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("requires unavailable part 月")));
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("assigned more than once")));
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("word 明日 requires unavailable 明")));
    }

    private static KanjiData kanji(String character, List<String> parts, List<String> recipe) {
        return new KanjiData(character, parts, recipe, character, "", "", "N5", 1);
    }
}
