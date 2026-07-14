package dev.paulatienza.kanjidiscovery.pipeline;

import dev.paulatienza.kanjidiscovery.pipeline.generation.N5WorldGenerator;
import dev.paulatienza.kanjidiscovery.pipeline.generation.PipelinePaths;
import dev.paulatienza.kanjidiscovery.pipeline.json.WorldDataJson;
import dev.paulatienza.kanjidiscovery.pipeline.kanjivg.KanjiVgCopier;
import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import dev.paulatienza.kanjidiscovery.pipeline.scene.SceneDataJson;
import dev.paulatienza.kanjidiscovery.pipeline.validation.SceneValidator;
import dev.paulatienza.kanjidiscovery.pipeline.validation.ValidationResult;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PipelineMain {
    private PipelineMain() {
    }

    public static void main(String[] args) {
        int status = run(args, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length < 1 || args.length > 2
                || !("generate".equals(args[0]) || "validate".equals(args[0]))) {
            err.println("Usage: PipelineMain <generate|validate> [project-root]");
            return 2;
        }
        Path root = args.length == 2 ? Path.of(args[1]) : Path.of(".");
        PipelinePaths paths = PipelinePaths.fromProjectRoot(root);
        try {
            if ("generate".equals(args[0])) {
                generate(paths, out);
            } else {
                validate(paths, out);
            }
            return 0;
        } catch (Exception e) {
            err.println("Phase 1 pipeline failed: " + e.getMessage());
            return 1;
        }
    }

    private static void generate(PipelinePaths paths, PrintStream out) throws Exception {
        WorldData world = new N5WorldGenerator().generate(paths);
        Files.createDirectories(paths.worldOutput().getParent());
        WorldDataJson.write(paths.worldOutput(), world);
        new KanjiVgCopier().copy(paths.kanjivg(), paths.kanjivgOutput(),
                world.kanji().stream().map(KanjiData::character).toList());
        out.println("Generated " + paths.worldOutput());
        out.println("Copied " + world.kanji().size() + " KanjiVG SVGs to " + paths.kanjivgOutput());
        validate(paths, out);
    }

    private static void validate(PipelinePaths paths, PrintStream out) throws Exception {
        WorldData reloaded = WorldDataJson.read(paths.worldOutput());
        ValidationResult result = new SceneValidator().validate(
                reloaded, SceneDataJson.read(paths.scenes()), "N5");
        out.print(result.report());
        if (!result.valid()) {
            throw new IllegalStateException(String.join(System.lineSeparator(), result.errors()));
        }
    }
}
