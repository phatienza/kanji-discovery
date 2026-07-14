package dev.paulatienza.kanjidiscovery.pipeline.validation;

import dev.paulatienza.kanjidiscovery.pipeline.model.KanjiData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WordData;
import dev.paulatienza.kanjidiscovery.pipeline.model.WorldData;
import dev.paulatienza.kanjidiscovery.pipeline.scene.Scene;
import dev.paulatienza.kanjidiscovery.pipeline.scene.SceneData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SceneValidator {
    public ValidationResult validate(WorldData world, SceneData sceneData, String targetLevel) {
        Map<String, KanjiData> kanji = world.kanji().stream()
                .collect(Collectors.toMap(KanjiData::character, value -> value));
        Map<String, WordData> words = world.words().stream()
                .collect(Collectors.toMap(WordData::word, value -> value));
        Map<String, Integer> assignmentCounts = new HashMap<>();
        Set<String> obtainable = new HashSet<>();
        List<String> errors = new ArrayList<>();
        StringBuilder report = new StringBuilder("Coverage report\n");

        for (Scene scene : sceneData.scenes()) {
            int irregular = 0;
            for (String found : scene.find()) {
                assignmentCounts.merge(found, 1, Integer::sum);
                KanjiData data = kanji.get(found);
                if (data == null) {
                    errors.add("Scene " + scene.id() + " references unknown find kanji " + found);
                } else {
                    obtainable.add(found);
                    if (!data.parts().isEmpty()) {
                        irregular++;
                    }
                }
            }

            for (String crafted : scene.craft()) {
                assignmentCounts.merge(crafted, 1, Integer::sum);
                KanjiData data = kanji.get(crafted);
                if (data == null) {
                    errors.add("Scene " + scene.id() + " references unknown craft kanji " + crafted);
                    continue;
                }
                if (data.recipe() == null || data.recipe().isEmpty()) {
                    errors.add("Craft kanji " + crafted + " has no recipe");
                    continue;
                }
                boolean available = true;
                for (String part : data.recipe()) {
                    if (!obtainable.contains(part)) {
                        errors.add("Craft kanji " + crafted + " requires unavailable part " + part
                                + " in scene " + scene.id());
                        available = false;
                    }
                }
                if (available) {
                    obtainable.add(crafted);
                }
            }

            for (String word : scene.words()) {
                WordData data = words.get(word);
                if (data == null) {
                    errors.add("Scene " + scene.id() + " references unknown or non-common word " + word);
                    continue;
                }
                for (String part : data.parts()) {
                    if (!obtainable.contains(part)) {
                        errors.add("Scene " + scene.id() + " word " + word
                                + " requires unavailable " + part);
                    }
                }
            }

            report.append(scene.id()).append(": total=")
                    .append(scene.find().size() + scene.craft().size())
                    .append(" find=").append(scene.find().size())
                    .append(" craft=").append(scene.craft().size())
                    .append(" irregular=").append(irregular).append('\n');
        }

        List<String> targets = world.kanji().stream()
                .filter(value -> targetLevel.equals(value.jlpt()))
                .map(KanjiData::character)
                .sorted()
                .toList();
        int exact = 0;
        for (String target : targets) {
            int count = assignmentCounts.getOrDefault(target, 0);
            if (count == 0) {
                errors.add("Target " + target + " is missing from all scenes");
            } else if (count > 1) {
                errors.add("Target " + target + " is assigned more than once");
            } else {
                exact++;
            }
        }
        report.append(targetLevel).append(" coverage: ").append(exact).append('/')
                .append(targets.size()).append(" assigned exactly once\n");

        Map<String, Integer> totals = new LinkedHashMap<>();
        totals.put("find", sceneData.scenes().stream().mapToInt(scene -> scene.find().size()).sum());
        totals.put("craft", sceneData.scenes().stream().mapToInt(scene -> scene.craft().size()).sum());
        report.append("Totals: find=").append(totals.get("find"))
                .append(" craft=").append(totals.get("craft"))
                .append(" errors=").append(errors.size()).append('\n');

        return new ValidationResult(List.copyOf(errors), report.toString());
    }
}
