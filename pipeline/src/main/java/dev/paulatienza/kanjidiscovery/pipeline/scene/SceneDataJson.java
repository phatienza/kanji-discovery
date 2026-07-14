package dev.paulatienza.kanjidiscovery.pipeline.scene;

import dev.paulatienza.kanjidiscovery.pipeline.json.WorldDataJson;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class SceneDataJson {
    private SceneDataJson() {
    }

    public static SceneData read(Path path) throws IOException {
        SceneData data = WorldDataJson.mapper().readValue(path.toFile(), SceneData.class);
        if (data.scenes() == null || data.scenes().isEmpty()) {
            throw new IOException("Scene file contains no scenes: " + path);
        }
        Set<String> ids = new HashSet<>();
        for (Scene scene : data.scenes()) {
            if (scene.id() == null || scene.id().isBlank() || !ids.add(scene.id())) {
                throw new IOException("Scene IDs must be present and unique: " + scene.id());
            }
            if (scene.find() == null || scene.craft() == null || scene.words() == null) {
                throw new IOException("Scene arrays must be present: " + scene.id());
            }
            if (scene.unlockThreshold() <= 0 || scene.unlockThreshold() > 1) {
                throw new IOException("Invalid unlock threshold for scene " + scene.id());
            }
            assertUnique(scene.find(), "find", scene.id());
            assertUnique(scene.craft(), "craft", scene.id());
            assertUnique(scene.words(), "words", scene.id());
        }
        return data;
    }

    private static void assertUnique(java.util.List<String> values, String field, String scene)
            throws IOException {
        if (new HashSet<>(values).size() != values.size()) {
            throw new IOException("Duplicate value in " + field + " for scene " + scene);
        }
    }
}
