package com.hbm.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityItemsTest {

	private static final Path TSV = Path.of("src", "main", "resources", "hbm", "legacy", "identity-items.tsv");
	private static final Path CREATIVE = Path.of("src", "main", "resources", "hbm", "legacy", "creative.tsv");
	private static final Path ASSETS = Path.of("src", "main", "resources", "assets", "hbm");
	private static final Path IDENTITY = Path.of("data", "legacy", "identity-map.json");

	@BeforeAll
	static void requireData() {
		Assumptions.assumeTrue(Files.exists(TSV) && Files.exists(CREATIVE) && Files.exists(IDENTITY),
				"identity item data missing");
	}

	@Test
	void everyIdentityItemHasStackDefinitionAndMapping() throws Exception {
		Set<String> creative = new HashSet<>();
		for (String line : Files.readAllLines(CREATIVE, StandardCharsets.UTF_8)) {
			String[] parts = line.split("\t", -1);
			if (parts.length >= 4) {
				creative.add(parts[2] + "|" + parts[3]);
			}
		}

		JsonObject identity = new Gson().fromJson(
				Files.readString(IDENTITY, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("mappings");

		int rows = 0;
		for (String line : Files.readAllLines(TSV, StandardCharsets.UTF_8)) {
			if (line.isBlank()) {
				continue;
			}
			String[] parts = line.split("\t", -1);
			String path = parts[0];
			String meta = parts[1];
			String itemId = parts[2];
			rows++;

			assertTrue(itemId.startsWith("hbm:"), "item id namespace: " + itemId);
			assertTrue(Files.exists(ASSETS.resolve("items").resolve(itemId.substring("hbm:".length()) + ".json")),
					"missing item definition for " + itemId);
			assertTrue(creative.contains("item." + path + "|" + meta),
					"no creative stack for item." + path + "|" + meta);
			String legacyKey = "hbm:item." + path + "|" + meta;
			assertTrue(identity.has(legacyKey), "missing identity mapping for " + legacyKey);
			assertTrue(identity.get(legacyKey).getAsJsonObject().get("id").getAsString().equals(itemId),
					"identity target mismatch for " + legacyKey);
		}
		assertTrue(rows > 0, "identity item registry is empty");
	}
}
