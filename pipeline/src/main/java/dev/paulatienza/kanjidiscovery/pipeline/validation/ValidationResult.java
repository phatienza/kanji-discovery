package dev.paulatienza.kanjidiscovery.pipeline.validation;

import java.util.List;

public record ValidationResult(List<String> errors, String report) {
    public boolean valid() {
        return errors.isEmpty();
    }
}
