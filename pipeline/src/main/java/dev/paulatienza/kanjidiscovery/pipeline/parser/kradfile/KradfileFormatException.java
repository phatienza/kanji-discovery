package dev.paulatienza.kanjidiscovery.pipeline.parser.kradfile;

import java.io.IOException;

public final class KradfileFormatException extends IOException {
    public KradfileFormatException(String message) {
        super(message);
    }

    public KradfileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
