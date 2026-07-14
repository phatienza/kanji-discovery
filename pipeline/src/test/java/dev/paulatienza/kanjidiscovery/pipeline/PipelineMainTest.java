package dev.paulatienza.kanjidiscovery.pipeline;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineMainTest {
    @Test
    void rejectsUnknownCommand() {
        int status = PipelineMain.run(new String[]{"unknown"},
                new PrintStream(new ByteArrayOutputStream()),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(2, status);
    }
}
