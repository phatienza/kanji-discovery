package dev.paulatienza.kanjidiscovery.pipeline.scene;

import java.util.List;

public record Scene(
        String id,
        String title,
        String backdrop,
        List<String> find,
        List<String> craft,
        List<String> words,
        double unlockThreshold) {
}
