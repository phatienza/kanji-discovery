package dev.paulatienza.kanjidiscovery.pipeline.parser.kanjidic2;

import java.io.IOException;

public final class KanjidicFormatException extends IOException {
    public KanjidicFormatException(String message) {
        super(message);
    }

    public KanjidicFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
