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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialRegistryTest {

	private static final Path TSV = Path.of("src", "main", "resources", "hbm", "legacy", "material-items.tsv");
	private static final Path CREATIVE = Path.of("src", "main", "resources", "hbm", "legacy", "creative.tsv");
	private static final Path ASSETS = Path.of("src", "main", "resources", "assets", "hbm");
	private static final Path IDENTITY = Path.of("data", "legacy", "identity-map.json");

	@BeforeAll
	static void requireData() {
		Assumptions.assumeTrue(Files.exists(TSV) && Files.exists(CREATIVE) && Files.exists(IDENTITY),
				"material registry data missing");
	}

	@Test
	void everyMaterialItemHasStackDefinitionAndIdentity() throws Exception {
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
		int mapped = 0;
		int definitions = 0;
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
			String defPath = itemId.substring("hbm:".length());
			assertTrue(Files.exists(ASSETS.resolve("items").resolve(defPath + ".json")),
					"missing item definition for " + itemId);
			definitions++;

			String legacyKey = "hbm:item." + path + "|" + meta;
			if (creative.contains("item." + path + "|" + meta)) {
				assertTrue(identity.has(legacyKey), "missing identity mapping for " + legacyKey);
				assertTrue(identity.get(legacyKey).getAsJsonObject().get("id").getAsString().equals(itemId),
						"identity target mismatch for " + legacyKey);
				mapped++;
			}
		}
		assertTrue(rows > 0, "material registry is empty");
		assertTrue(definitions == rows, "every material item must have a definition");
		assertTrue(mapped > 0, "no material item maps to a creative stack");
		assertFalse(identity.entrySet().isEmpty(), "identity map is empty");
	}

	@Test
	void tagsOnlyReferenceRegisteredItems() throws Exception {
		Set<String> registered = new HashSet<>();
		for (String line : Files.readAllLines(TSV, StandardCharsets.UTF_8)) {
			if (!line.isBlank()) {
				registered.add(line.split("\t", -1)[2]);
			}
		}

		Gson gson = new Gson();
		Path tags = Path.of("src", "main", "resources", "data", "hbm", "tags", "item");
		Assumptions.assumeTrue(Files.exists(tags), "no generated tags");
		int tagFiles = 0;
		try (var stream = Files.walk(tags)) {
			for (Path tag : stream.filter(path -> path.toString().endsWith(".json")).toList()) {
				tagFiles++;
				JsonObject body = gson.fromJson(Files.readString(tag, StandardCharsets.UTF_8), JsonObject.class);
				for (var value : body.getAsJsonArray("values")) {
					assertTrue(registered.contains(value.getAsString()),
							"tag " + tag.getFileName() + " references unregistered " + value.getAsString());
				}
			}
		}
		assertTrue(tagFiles > 0, "expected generated tag files");
	}
}
