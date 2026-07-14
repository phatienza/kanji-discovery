package dev.paulatienza.kanjidiscovery.pipeline.parser.jmdict;

import java.io.IOException;

public final class JmdictFormatException extends IOException {
    public JmdictFormatException(String message) {
        super(message);
    }

    public JmdictFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
