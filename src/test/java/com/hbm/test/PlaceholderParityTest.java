package com.hbm.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderParityTest {

	private static final Path DUMP = Path.of("data", "legacy", "legacy-items.json");
	private static final Path TSV = Path.of("src", "main", "resources", "hbm", "legacy", "creative.tsv");
	private static final Path COLLAPSE = Path.of("tools", "generate-placeholder", "collapse.json");

	@BeforeAll
	static void requireData() {
		Assumptions.assumeTrue(Files.exists(DUMP) && Files.exists(TSV), "placeholder data missing");
	}

	@Test
	void creativeTsvMatchesLegacyDump() throws Exception {
		Map<String, String> collapse = collapseRules();
		JsonObject dump = new Gson().fromJson(Files.readString(DUMP, StandardCharsets.UTF_8), JsonObject.class);

		Map<String, List<JsonObject>> byTab = new java.util.LinkedHashMap<>();
		for (JsonElement element : dump.getAsJsonArray("items")) {
			JsonObject row = element.getAsJsonObject();
			if (!row.get("id").getAsString().startsWith("hbm:") || row.get("tab") == null || row.get("tab").isJsonNull()) {
				continue;
			}
			byTab.computeIfAbsent(row.get("tab").getAsString(), key -> new ArrayList<>()).add(row);
		}

		List<String> expected = new ArrayList<>();
		for (Map.Entry<String, List<JsonObject>> entry : byTab.entrySet()) {
			int order = 0;
			List<String> seen = new ArrayList<>();
			for (JsonObject row : entry.getValue()) {
				String path = row.get("id").getAsString().split(":", 2)[1];
				String display = display(row);
				int meta = row.get("meta").getAsInt();
				if (collapse.containsKey(path)) {
					if (seen.contains(path)) {
						continue;
					}
					seen.add(path);
					display = collapse.get(path);
					meta = 0;
				}
				expected.add(entry.getKey() + "\t" + order + "\t" + path + "\t" + meta + "\t" + display);
				order++;
			}
		}

		List<String> actual = new ArrayList<>();
		for (String line : Files.readAllLines(TSV, StandardCharsets.UTF_8)) {
			if (line.isBlank()) {
				continue;
			}
			String[] parts = line.split("\t", -1);
			actual.add(String.join("\t", java.util.Arrays.copyOf(parts, 5)));
		}

		assertEquals(expected.size(), actual.size(), "creative.tsv entry count");
		assertEquals(expected, actual, "creative.tsv content");
	}

	private static String display(JsonObject row) {
		return row.has("display") && !row.get("display").isJsonNull() ? row.get("display").getAsString() : "";
	}

	private static Map<String, String> collapseRules() throws Exception {
		Map<String, String> rules = new HashMap<>();
		if (!Files.exists(COLLAPSE)) {
			return rules;
		}
		JsonObject root = new Gson().fromJson(Files.readString(COLLAPSE, StandardCharsets.UTF_8), JsonObject.class);
		JsonObject collapse = root.getAsJsonObject("collapse");
		for (Map.Entry<String, JsonElement> entry : collapse.entrySet()) {
			rules.put(entry.getKey(), entry.getValue().getAsString());
		}
		return rules;
	}
}
