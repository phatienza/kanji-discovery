package dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2;

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
import java.util.List;
import java.util.Map;

public final class Kanjidic2Parser {
    public KanjidicParseResult parse(Path path) throws IOException {
        XMLInputFactory factory = secureFactory();
        Map<String, KanjidicEntry> entries = new LinkedHashMap<>();
        int total = 0;

        try (InputStream input = Files.newInputStream(path)) {
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            Builder builder = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("character".equals(name)) {
                        builder = new Builder();
                        total++;
                    } else if (builder != null) {
                        switch (name) {
                            case "literal" -> builder.character = reader.getElementText().trim();
                            case "grade" -> builder.grade = integer(reader.getElementText(), "grade", total);
                            case "jlpt" -> builder.jlpt = integer(reader.getElementText(), "jlpt", total);
                            case "stroke_count" -> {
                                if (builder.strokes == 0) {
                                    builder.strokes = integer(reader.getElementText(), "stroke_count", total);
                                } else {
                                    reader.getElementText();
                                }
                            }
                            case "freq" -> builder.frequency = integer(reader.getElementText(), "freq", total);
                            case "reading" -> {
                                String type = reader.getAttributeValue(null, "r_type");
                                String value = reader.getElementText().trim();
                                if ("ja_on".equals(type)) {
                                    builder.on.add(value);
                                } else if ("ja_kun".equals(type)) {
                                    builder.kun.add(value);
                                }
                            }
                            case "meaning" -> {
                                String language = reader.getAttributeValue(null, "m_lang");
                                String value = reader.getElementText().trim();
                                if (language == null || "en".equals(language)) {
                                    builder.meanings.add(value);
                                }
                            }
                            default -> {
                            }
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                        && "character".equals(reader.getLocalName())) {
                    KanjidicEntry entry = requireEntry(builder, total);
                    if (entries.putIfAbsent(entry.character(), entry) != null) {
                        throw new KanjidicFormatException("Duplicate KANJIDIC2 literal: " + entry.character());
                    }
                    builder = null;
                }
            }
            reader.close();
        } catch (XMLStreamException | NumberFormatException e) {
            throw new KanjidicFormatException("Failed to parse KANJIDIC2 " + path + ": " + e.getMessage(), e);
        }

        return new KanjidicParseResult(Map.copyOf(entries), new ParseStats(total, entries.size(), 0));
    }

    private static XMLInputFactory secureFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        return factory;
    }

    private static int integer(String value, String field, int entryNumber) throws KanjidicFormatException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new KanjidicFormatException("Invalid " + field + " in character entry " + entryNumber, e);
        }
    }

    private static KanjidicEntry requireEntry(Builder builder, int entryNumber) throws KanjidicFormatException {
        if (builder == null || builder.character == null || builder.character.isBlank()) {
            throw new KanjidicFormatException("Character entry " + entryNumber + " has no literal");
        }
        if (builder.strokes <= 0) {
            throw new KanjidicFormatException("Character " + builder.character + " has no valid stroke count");
        }
        return new KanjidicEntry(builder.character, builder.grade, builder.jlpt, builder.strokes,
                builder.frequency, List.copyOf(builder.on), List.copyOf(builder.kun),
                List.copyOf(builder.meanings));
    }

    private static final class Builder {
        private String character;
        private Integer grade;
        private Integer jlpt;
        private int strokes;
        private Integer frequency;
        private final List<String> on = new ArrayList<>();
        private final List<String> kun = new ArrayList<>();
        private final List<String> meanings = new ArrayList<>();
    }
}
