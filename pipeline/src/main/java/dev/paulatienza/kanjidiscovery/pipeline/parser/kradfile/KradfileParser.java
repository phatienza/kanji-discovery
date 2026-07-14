package dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile;

import dev.paulatienza.kanjidiscovery.pipeline.parser.ParseStats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KradfileParser {
    public static final Charset KRADFILE_CHARSET = Charset.forName("EUC-JP");
    private static final Pattern DATA_LINE = Pattern.compile("^(\\S+)\\s+:\\s+(.+)$");

    public KradParseResult parse(Path path) throws IOException {
        Map<String, KradEntry> entries = new LinkedHashMap<>();
        int lineNumber = 0;
        var decoder = KRADFILE_CHARSET.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Files.newInputStream(path), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                Matcher matcher = DATA_LINE.matcher(line);
                if (!matcher.matches()) {
                    throw new KradfileFormatException("Malformed KRADFILE line " + lineNumber + ": " + line);
                }
                String character = matcher.group(1);
                List<String> parts = Arrays.stream(matcher.group(2).trim().split("\\s+"))
                        .filter(part -> !part.isBlank())
                        .toList();
                if (parts.isEmpty()) {
                    throw new KradfileFormatException("KRADFILE line " + lineNumber + " has no parts");
                }
                if (entries.putIfAbsent(character, new KradEntry(character, parts)) != null) {
                    throw new KradfileFormatException("Duplicate KRADFILE character at line " + lineNumber + ": " + character);
                }
            }
        } catch (CharacterCodingException e) {
            throw new KradfileFormatException("KRADFILE is not valid EUC-JP: " + path, e);
        }
        return new KradParseResult(Map.copyOf(entries), new ParseStats(entries.size(), entries.size(), 0));
    }
}
