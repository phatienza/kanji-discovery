package dev.paulatienza.kanjidiscovery.pipeline.generation;

import java.nio.file.Path;

public record PipelinePaths(
        Path projectRoot,
        Path kanjidic2,
        Path kradfile,
        Path jmdict,
        Path kanjivg,
        Path scenes,
        Path variants,
        Path recipes,
        Path worldOutput,
        Path kanjivgOutput) {

    public static PipelinePaths fromProjectRoot(Path projectRoot) {
        Path root = projectRoot.toAbsolutePath().normalize();
        return new PipelinePaths(
                root,
                root.resolve("data/raw/kanjidic2.xml"),
                root.resolve("data/raw/kradfile"),
                root.resolve("data/raw/JMdict_e"),
                root.resolve("data/raw/kanjivg"),
                root.resolve("scenes/n5-scenes.json"),
                root.resolve("curation/radical-variants.tsv"),
                root.resolve("curation/n5-recipes.tsv"),
                root.resolve("data/n5.json"),
                root.resolve("app/public/assets/kanjivg/n5"));
    }
}
