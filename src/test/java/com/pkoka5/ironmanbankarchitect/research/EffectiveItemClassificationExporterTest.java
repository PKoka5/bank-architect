package com.pkoka5.ironmanbankarchitect.research;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class EffectiveItemClassificationExporterTest
{
	@Test
	public void exportsEffectiveCatalogRoutesAndSeparatesCacheOnlyConstants() throws Exception
	{
		Path directory = Files.createTempDirectory("bank-architect-effective-export");
		Path registry = directory.resolve("registry.tsv");
		Path output = directory.resolve("effective.tsv");
		Path excluded = directory.resolve("excluded.tsv");
		Files.write(registry, (
			"\uFEFF145\tSuper attack (3)\tPOTION\tSUPER_ATTACK3\n" +
			"3841\tDamaged book\tHERBLORE\tUNFINISHED_ZAMORAKBOOK\n" +
			"900001\tRestricted variant\tGEAR\tWEAPON_BR_VARIANT\n" +
			"900002\tCrossbow cache name\tGEAR\tCROSSBOW_BRONZE\n" +
			"900000\tInterface thing\tUNKNOWN\tBANK_INTERFACE_DUMMY\n")
			.getBytes(StandardCharsets.UTF_8));

		EffectiveItemClassificationExporter.ExportStats stats =
			EffectiveItemClassificationExporter.export(registry, output, excluded);

		assertEquals(4, stats.included);
		assertEquals(1, stats.excluded);
		List<String> rows = Files.readAllLines(output, StandardCharsets.UTF_8);
		assertTrue(rows.stream().anyMatch(row -> row.startsWith("145\tSuper attack (3)\tPOTION\tdose-3\therblore\therblore.super-attack.3\t")));
		assertTrue(rows.stream().anyMatch(row -> row.startsWith("3841\tDamaged book\tGEAR\tshield\tcombat-gear\t\t")));
		assertTrue(rows.stream().anyMatch(row -> row.startsWith("900001\tUnknown item #900001\t")
			&& row.endsWith("\tbattle-royale")));
		assertTrue(rows.stream().anyMatch(row -> row.startsWith("900002\tUnknown item #900002\t")
			&& row.endsWith("\t")));
		assertTrue(Files.readAllLines(excluded, StandardCharsets.UTF_8).stream()
			.anyMatch(row -> row.startsWith("900000\tInterface thing\tBANK_INTERFACE_DUMMY\t")));
	}
}
