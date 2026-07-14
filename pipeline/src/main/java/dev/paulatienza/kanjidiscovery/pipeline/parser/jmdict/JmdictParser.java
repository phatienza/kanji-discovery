package dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict;

import dev.paulatienza.kanjidiscovery.pipeline.parser.ParseStats;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JmdictParser {
    public JmdictParseResult parse(Path path, Set<String> requestedWords) throws IOException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

        Map<String, JmdictWord> accepted = new LinkedHashMap<>();
        int total = 0;
        try (InputStream input = Files.newInputStream(path)) {
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            EntryBuilder entry = null;
            OrthographyBuilder orthography = null;
            ReadingBuilder reading = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    switch (name) {
                        case "entry" -> {
                            entry = new EntryBuilder();
                            total++;
                        }
                        case "k_ele" -> orthography = new OrthographyBuilder();
                        case "r_ele" -> reading = new ReadingBuilder();
                        case "keb" -> orthography.word = reader.getElementText().trim();
                        case "ke_pri" -> orthography.priorities.add(reader.getElementText().trim());
                        case "reb" -> reading.value = reader.getElementText().trim();
                        case "re_restr" -> reading.restrictions.add(reader.getElementText().trim());
                        case "re_pri" -> reading.priorities.add(reader.getElementText().trim());
                        case "gloss" -> {
                            String language = reader.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
                            String value = reader.getElementText().trim();
                            if (entry != null && (language == null || "eng".equals(language) || "en".equals(language))) {
                                entry.glosses.add(value);
                            }
                        }
                        default -> {
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("k_ele".equals(name)) {
                        requireOrthography(orthography, total);
                        entry.orthographies.add(orthography);
                        orthography = null;
                    } else if ("r_ele".equals(name)) {
                        requireReading(reading, total);
                        entry.readings.add(reading);
                        reading = null;
                    } else if ("entry".equals(name)) {
                        acceptRequested(entry, requestedWords, accepted, total);
                        entry = null;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new JmdictFormatException("Failed to parse JMdict " + path + ": " + e.getMessage(), e);
        }
        return new JmdictParseResult(Map.copyOf(accepted),
                new ParseStats(total, accepted.size(), total - accepted.size()));
    }

    private static void acceptRequested(EntryBuilder entry, Set<String> requested,
                                        Map<String, JmdictWord> accepted, int entryNumber)
            throws JmdictFormatException {
        if (entry == null || entry.readings.isEmpty() || entry.glosses.isEmpty()) {
            throw new JmdictFormatException("JMdict entry " + entryNumber + " is missing readings or glosses");
        }
        for (OrthographyBuilder orthography : entry.orthographies) {
            if (!requested.contains(orthography.word) || accepted.containsKey(orthography.word)) {
                continue;
            }
            List<ReadingBuilder> compatible = entry.readings.stream()
                    .filter(candidate -> candidate.restrictions.isEmpty()
                            || candidate.restrictions.contains(orthography.word))
                    .toList();
            if (compatible.isEmpty()) {
                throw new JmdictFormatException("No compatible reading for requested word " + orthography.word);
            }
            ReadingBuilder selected = compatible.stream()
                    .filter(candidate -> !candidate.priorities.isEmpty())
                    .findFirst()
                    .orElse(compatible.get(0));
            boolean common = !orthography.priorities.isEmpty() || !selected.priorities.isEmpty();
            if (common) {
                accepted.put(orthography.word,
                        new JmdictWord(orthography.word, selected.value, entry.glosses.get(0)));
            }
        }
    }

    private static void requireOrthography(OrthographyBuilder builder, int entry) throws JmdictFormatException {
        if (builder == null || builder.word == null || builder.word.isBlank()) {
            throw new JmdictFormatException("JMdict entry " + entry + " has an invalid k_ele");
        }
    }

    private static void requireReading(ReadingBuilder builder, int entry) throws JmdictFormatException {
        if (builder == null || builder.value == null || builder.value.isBlank()) {
            throw new JmdictFormatException("JMdict entry " + entry + " has an invalid r_ele");
        }
    }

    private static final class EntryBuilder {
        private final List<OrthographyBuilder> orthographies = new ArrayList<>();
        private final List<ReadingBuilder> readings = new ArrayList<>();
        private final List<String> glosses = new ArrayList<>();
    }

    private static final class OrthographyBuilder {
        private String word;
        private final Set<String> priorities = new LinkedHashSet<>();
    }

    private static final class ReadingBuilder {
        private String value;
        private final Set<String> restrictions = new LinkedHashSet<>();
        private final Set<String> priorities = new LinkedHashSet<>();
    }
}
