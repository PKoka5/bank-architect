package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.Test;

/** Pins public behavior captured from compiled classes before the bundled-definition migration. */
public class BankDefinitionCompatibilityTest
{
    private static final List<String> BASELINE = loadBaseline();

    @Test
    public void everyPresetIdentityAndOrderedCategoryKeepsItsPreviousNameAndSortMode()
    {
        List<String> actual = new ArrayList<>();
        for (BankPresetType type : BankPresetType.values())
        {
            BankPreset preset = BankPresets.forType(type);
            actual.add(row("PRESET", type, preset.getKey(), preset.getName()));
            int index = 0;
            for (BankCategory category : preset.getCategories())
            {
                actual.add(row("CATEGORY", type, index++, category.getKey(), category.getName(),
                    category.getSortMode()));
            }
        }
        assertEquals(baseline("PRESET", "CATEGORY"), actual);
    }

    @Test
    public void everyTagKeepsItsPreviousNameDeclarationOrderCategoryAndLookup()
    {
        List<String> actual = new ArrayList<>();
        int index = 0;
        for (BankTag tag : BankTags.all())
        {
            actual.add(row("TAG", index++, tag.getKey(), tag.getName(), tag.getCategoryKey()));
            BankTag lookup = BankTags.byKey(tag.getKey());
            assertTrue(BankTags.isKnown(tag.getKey()));
            assertEquals(tag.getName(), lookup.getName());
            assertEquals(tag.getCategoryKey(), lookup.getCategoryKey());
        }
        assertEquals(baseline("TAG"), actual);
        for (String line : baseline("FALLBACK"))
        {
            String category = line.split("\t", -1)[1];
            List<String> expectedKeys = baseline("TAG").stream()
                .map(value -> value.split("\t", -1))
                .filter(fields -> fields[4].equals(category))
                .map(fields -> fields[2]).collect(Collectors.toList());
            assertEquals(category, expectedKeys, BankTags.forCategory(category).stream()
                .map(BankTag::getKey).collect(Collectors.toList()));
        }
    }

    @Test
    public void everyDeclaredSubcategoryAndCatchAllKeepsItsPreviousDestination()
    {
        for (String line : baseline("ROUTE"))
        {
            String[] fields = line.split("\t", -1);
            assertEquals(line, fields[3], BankTags.tagFor(fields[1], fields[2]).getKey());
            assertEquals(line, fields[3], BankTags.tagFor(fields[1],
                "  " + fields[2].toUpperCase(Locale.ROOT) + "  ").getKey());
        }
        for (String line : baseline("FALLBACK"))
        {
            String[] fields = line.split("\t", -1);
            for (String subcategory : new String[] {null, "", "__uncategorized_compatibility__"})
            {
                assertEquals(line, fields[2], BankTags.tagFor(fields[1], subcategory).getKey());
            }
        }
    }

    private static List<String> baseline(String... kinds)
    {
        return BASELINE.stream().filter(line -> Arrays.asList(kinds)
            .contains(line.split("\t", -1)[0])).collect(Collectors.toList());
    }

    private static String row(Object... fields)
    {
        return Arrays.stream(fields).map(String::valueOf).collect(Collectors.joining("\t"));
    }

    private static List<String> loadBaseline()
    {
        InputStream stream = BankDefinitionCompatibilityTest.class
            .getResourceAsStream("bank-definition-compatibility.tsv");
        assertNotNull("Pre-migration public-behavior fixture", stream);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
        {
            return reader.lines().filter(line -> !line.startsWith("#")).collect(Collectors.toList());
        }
        catch (IOException ex)
        {
            throw new AssertionError("Cannot read the pre-migration public-behavior fixture", ex);
        }
    }
}
