package dev.paulatienza.kanjidiscovery.pipeline.curation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariantTableTest {
    @Test
    void normalizesUnicodeAndKradfilePersonVariants(@TempDir Path tempDir) throws Exception {
        Path table = tempDir.resolve("variants.tsv");
        Files.writeString(table, "variant\tcanonical\tstatus\tnote\n亻\t人\tCONFIRMED\tunicode\n化\t人\tTODO\tkrad placeholder\n");

        VariantTable variants = VariantTable.load(table);

        assertEquals("人", variants.canonicalize("亻"));
        assertEquals("人", variants.canonicalize("化"));
        assertEquals("木", variants.canonicalize("木"));
    }

    @Test
    void loadsOrderedRecipesAndRejectsDuplicateKeys(@TempDir Path tempDir) throws Exception {
        Path recipes = tempDir.resolve("recipes.tsv");
        Files.writeString(recipes, "kanji\trecipe\tstatus\tnote\n林\t木,木\tCONFIRMED\tforest\n休\t人,木\tCONFIRMED\trest\n");

        RecipeTable table = RecipeTable.load(recipes);

        assertEquals(java.util.List.of("木", "木"), table.recipeFor("林"));
        assertEquals(null, table.recipeFor("曜"));

        Files.writeString(recipes, "kanji\trecipe\tstatus\tnote\n林\t木,木\tCONFIRMED\tfirst\n林\t林,木\tTODO\tduplicate\n");
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> RecipeTable.load(recipes));
    }
}
