package dev.paulatienza.kanjidiscovery.pipeline.scene;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SceneDataJsonTest {
    @Test
    void readsTheTenSceneDraftInOrder() throws Exception {
        SceneData data = SceneDataJson.read(Path.of("../scenes/n5-scenes.json"));

        assertEquals(10, data.scenes().size());
        assertEquals("sky", data.scenes().get(0).id());
        assertEquals(java.util.List.of("吾", "語", "話"), data.scenes().get(7).craft());
        assertEquals(0.7, data.scenes().get(9).unlockThreshold());
    }
}
