package dev.paulatienza.kanjidiscovery.pipeline.generation;

import dev.paulatienza.kanjidiscovery.pipeline.curation.RecipeTable;
import dev.paulatienza.kanjidiscovery.pipeline.curation.VariantTable;
import dev.paulatienza.kanjidiscovery.pipeline.model.ComponentData;
import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WordData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict.JmdictParseResult;
import dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict.JmdictParser;
import dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict.JmdictWord;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2.Kanjidic2Parser;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2.KanjidicEntry;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2.KanjidicParseResult;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile.KradEntry;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile.KradParseResult;
import dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile.KradfileParser;
import dev.paulatienza.kanjidiscovery.pipeline.scene.Scene;
import dev.paulatienza.kanjidiscovery.pipeline.scene.SceneData;
import dev.paulatienza.kanjidiscovery.pipeline.scene.SceneDataJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class N5WorldGenerator {
    private static final int COMMON_COMPONENT_COUNT = 200;

    public WorldData generate(PipelinePaths paths) throws IOException {
        KanjidicParseResult kanjidic = new Kanjidic2Parser().parse(paths.kanjidic2());
        KradParseResult krad = new KradfileParser().parse(paths.kradfile());
        VariantTable variants = VariantTable.load(paths.variants());
        RecipeTable recipes = RecipeTable.load(paths.recipes());
        SceneData scenes = SceneDataJson.read(paths.scenes());

        LinkedHashSet<String> included = new LinkedHashSet<>();
        kanjidic.entries().values().stream()
                .filter(this::isN5Target)
                .map(KanjidicEntry::character)
                .forEach(included::add);
        for (Scene scene : scenes.scenes()) {
            included.addAll(scene.find());
            included.addAll(scene.craft());
        }

        LinkedHashSet<String> requestedWords = new LinkedHashSet<>();
        scenes.scenes().forEach(scene -> requestedWords.addAll(scene.words()));
        JmdictParseResult jmdict = new JmdictParser().parse(paths.jmdict(), requestedWords);
        Set<String> missingWords = new LinkedHashSet<>(requestedWords);
        missingWords.removeAll(jmdict.entries().keySet());
        if (!missingWords.isEmpty()) {
            throw new IOException("Scene words missing or not common in JMdict: " + missingWords);
        }

        List<KanjiData> kanjiData = new ArrayList<>();
        Map<String, List<String>> normalizedParts = new HashMap<>();
        for (String character : included) {
            KanjidicEntry source = requireKanji(kanjidic.entries(), character);
            KradEntry decomposition = krad.entries().get(character);
            if (decomposition == null) {
                throw new IOException("No KRADFILE decomposition for included kanji " + character);
            }
            List<String> parts = normalizeParts(character, decomposition.parts(), variants);
            normalizedParts.put(character, parts);
            List<String> recipe = recipes.recipeFor(character);
            kanjiData.add(new KanjiData(
                    character,
                    parts,
                    recipe,
                    join(source.meanings(), 2),
                    join(source.onReadings(), 2),
                    join(source.kunReadings(), 2),
                    mapJlpt(source.legacyJlpt()),
                    source.strokes()));
        }
        kanjiData.sort(Comparator.comparing(KanjiData::character));

        for (Scene scene : scenes.scenes()) {
            for (String crafted : scene.craft()) {
                if (recipes.recipeFor(crafted) == null) {
                    throw new IOException("Scene craft kanji has no curated recipe: " + crafted);
                }
            }
        }

        List<ComponentData> components = components(
                kanjidic.entries(), krad.entries(), variants, normalizedParts, recipes);
        List<WordData> words = words(requestedWords, jmdict.entries());
        return new WorldData(components, kanjiData, words);
    }

    private List<ComponentData> components(Map<String, KanjidicEntry> kanjidic,
                                           Map<String, KradEntry> krad,
                                           VariantTable variants,
                                           Map<String, List<String>> includedParts,
                                           RecipeTable recipes) {
        Map<String, Integer> frequency = new HashMap<>();
        for (KradEntry entry : krad.values()) {
            KanjidicEntry source = kanjidic.get(entry.character());
            if (source == null || source.grade() == null || source.grade() < 1 || source.grade() > 8) {
                continue;
            }
            for (String part : normalizeParts(entry.character(), entry.parts(), variants)) {
                frequency.merge(part, 1, Integer::sum);
            }
        }

        LinkedHashSet<String> selected = new LinkedHashSet<>();
        frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(COMMON_COMPONENT_COUNT)
                .map(Map.Entry::getKey)
                .forEach(selected::add);
        includedParts.values().forEach(selected::addAll);
        recipes.entries().values().forEach(entry -> selected.addAll(entry.recipe()));

        List<ComponentData> result = new ArrayList<>();
        for (String component : selected) {
            KanjidicEntry source = kanjidic.get(component);
            result.add(new ComponentData(
                    component,
                    source == null ? "" : join(source.meanings(), 2),
                    source == null ? "" : join(source.onReadings(), 2),
                    source == null ? "" : join(source.kunReadings(), 2),
                    source == null ? 0 : source.strokes()));
        }
        return List.copyOf(result);
    }

    private static List<WordData> words(LinkedHashSet<String> requested,
                                        Map<String, JmdictWord> parsed) {
        List<WordData> result = new ArrayList<>();
        for (String word : requested) {
            JmdictWord source = parsed.get(word);
            result.add(new WordData(word, codePoints(word), source.reading(), source.meaning(), "N5"));
        }
        return List.copyOf(result);
    }

    static List<String> normalizeParts(String character, List<String> parts, VariantTable variants) {
        return parts.stream()
                .map(variants::canonicalize)
                .filter(part -> !part.equals(character))
                .toList();
    }

    private boolean isN5Target(KanjidicEntry entry) {
        return entry.legacyJlpt() != null && entry.legacyJlpt() == 4
                && entry.grade() != null && entry.grade() >= 1 && entry.grade() <= 8;
    }

    private static KanjidicEntry requireKanji(Map<String, KanjidicEntry> entries, String character)
            throws IOException {
        KanjidicEntry entry = entries.get(character);
        if (entry == null) {
            throw new IOException("Scene references character missing from KANJIDIC2: " + character);
        }
        return entry;
    }

    private static String mapJlpt(Integer legacy) {
        if (legacy == null) {
            return "";
        }
        return switch (legacy) {
            case 4 -> "N5";
            case 3 -> "N4";
            case 2 -> "N2";
            case 1 -> "N1";
            default -> "";
        };
    }

    private static String join(List<String> values, int limit) {
        return String.join(", ", values.stream().limit(limit).toList());
    }

    private static List<String> codePoints(String value) {
        return value.codePoints().mapToObj(codePoint -> new String(Character.toChars(codePoint))).toList();
    }
}
